package org.spliffy.sync;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotFoundException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.io.IOUtils;
import org.hashsplit4j.api.*;
import org.spliffy.common.HashUtils;

/**
 *
 * @author brad
 */
public class Syncer {

    private final HttpHashStore httpHashStore;
    private final HttpBlobStore httpBlobStore;    
    private final HttpClient client;
    private final Archiver archiver;
    private final File root;
    private final Path baseUrl;

    public Syncer(File root, HttpHashStore httpHashStore, HttpBlobStore httpBlobStore, HttpClient client, Archiver archiver, String baseUrl) {
        this.root = root;
        this.httpHashStore = httpHashStore;
        this.httpBlobStore = httpBlobStore;
        this.archiver = archiver;
        this.client = client;
        this.baseUrl = Path.path(baseUrl);
    }

    
    public void createRemoteDir(Path path) throws ConflictException {
        String href = HttpUtils.toHref(baseUrl, path);        
        HttpUtils.mkcol(client, href);
    }

    public void deleteRemote(Path path)  {
        String s = HttpUtils.toHref(this.baseUrl, path);
        try {
            HttpUtils.delete(client, s);
        } catch (NotFoundException ex) {
            System.out.println("not found: " + s + " but ignoring as we only wanted to delete it anyway");
        }
    }
    
    
    public void downloadSync(long hash, Path path) throws IOException {
        System.out.println("downloadSync: " + path);
        File localFile = toFile(path);
        List<HashStore> hashStores = new ArrayList<>();
        List<BlobStore> blobStores = new ArrayList<>();

        // If we have a temp file it means a previous download didnt complete. Lets
        // use it as a store so we can resume the download
        File fTemp = new File(localFile.getAbsolutePath() + ".new.tmp");
        FileBlobStore partialDownloadBlobStore = null;
        FileBlobStore oldFileBlobStore = null;
        try {
            if (fTemp.exists()) {
                // found previous download file, so use it as a hashstore
                MemoryHashStore partialDownloadHashStore = new MemoryHashStore();
                partialDownloadBlobStore = new FileBlobStore(fTemp);
                partialDownloadBlobStore.openForRead();
                Parser.parse(fTemp, partialDownloadBlobStore, partialDownloadHashStore);
                hashStores.add(partialDownloadHashStore);
                blobStores.add(partialDownloadBlobStore);
            }

            // Also use the current file (if it exists!) as a hash and blob store, since we're hoping
            // much of the file is unchanged
            if (localFile.exists()) {
                MemoryHashStore oldFileHashStore = new MemoryHashStore();
                oldFileBlobStore = new FileBlobStore(localFile);
                oldFileBlobStore.openForRead();
                Parser.parse(localFile, oldFileBlobStore, oldFileHashStore);
                hashStores.add(oldFileHashStore);
                blobStores.add(oldFileBlobStore);
            }

            // Now add the remote stores, where we will download anything not present locally
            hashStores.add(httpHashStore);
            blobStores.add(httpBlobStore);

            HashStore multiHashStore = new MultipleHashStore(hashStores);
            BlobStore multiBlobStore = new MultipleBlobStore(blobStores);

            Fanout rootRemoteFanout = multiHashStore.getFanout(hash);
            if (rootRemoteFanout == null) {
                throw new RuntimeException("Coudlnt find remote hash: " + hash);
            }
            List<Long> rootHashes = rootRemoteFanout.getHashes();

            FileOutputStream fout = null;
            try {
                fout = new FileOutputStream(fTemp);
                try (BufferedOutputStream bufOut = new BufferedOutputStream(fout)) {
                    Combiner combiner = new Combiner();
                    // TODO: Use MultipleBlobStore with the httpHashStore and LocalFileTriplet.blobStore to minimise network traffic                
                    combiner.combine(rootHashes, multiHashStore, multiBlobStore, bufOut);
                    bufOut.flush();
                }
            } finally {
                IOUtils.closeQuietly(fout);
            }
        } finally {
            if (partialDownloadBlobStore != null) {
                partialDownloadBlobStore.close();
            }
            if (oldFileBlobStore != null) {
                oldFileBlobStore.close();
            }
        }

        // Verify the CRC
        HashUtils.verifyHash(fTemp, hash);

        // Downloaded to temp file, so now swap with real file
        if (localFile.exists()) {
            archiver.archive(localFile);
        }

        // Now rename the new file to the real file name
        if (!fTemp.renameTo(localFile)) {
            throw new RuntimeException("Downloaded update ok, and renamed old file, but failed to rename new file to original file name: " + localFile.getAbsolutePath());
        }

        System.out.println("Finished update!");
    }

//    public void deleteRemoteFile(String childEncodedPath) {
//        try {
//            HttpUtils.delete(client, childEncodedPath);
//        } catch (NotFoundException ex) {
//            //System.out.println("Not Found: " + childEncodedPath + " - ignoring exception because we were going to delete it anyway");
//        }
//    }
    
    public void upSync(Path path) throws FileNotFoundException, IOException {
        File file = toFile(path);
        System.out.println("upSync: " + file.getAbsolutePath());

        FileInputStream fin = null;
        try {
            fin = new FileInputStream(file);
            BufferedInputStream bufIn = new BufferedInputStream(fin);
            Parser parser = new Parser();
            long newHash = parser.parse(bufIn, httpHashStore, httpBlobStore);

            // Now set the new hash on the remote file, which effectively commits the new content
            updateHashOnRemoteResource(newHash, path);
        } finally {
            IOUtils.closeQuietly(fin);
        }
    }


    /**
     * Do a PUT with a special content type so the server knows to just update
     * the file's hash. The PUT content is just the hash
     *
     * @param hash
     * @param encodedPath
     */
    private void updateHashOnRemoteResource(long hash, Path path) {
        String s = HttpUtils.toHref(this.baseUrl, path);
        System.out.println("Syncer::updateHashOnRemoteResource: " + s);
        PutMethod p = new PutMethod(s);
        p.setRequestHeader("Content-Type", "spliffy/hash");

        // Copy longs into a byte array
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bout);
        try {
            dos.writeLong(hash); // send the actualContentLength first
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        byte[] data = bout.toByteArray();

        HttpMethodParams params = new HttpMethodParams();
        p.setParams(params);
        try {
            RequestEntity requestEntity = new ByteArrayRequestEntity(data);

            p.setRequestEntity(requestEntity);
            int result = client.executeMethod(p);
            if (result < 200 || result >= 300) {
                throw new RuntimeException("Hash set failed. result:" + result + "  url: " + s);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } finally {
            p.releaseConnection();
        }
    }

    
    public Path getBaseUrl() {
        return baseUrl;
    }

    private File toFile(Path path) {
        File f = root;
        for (String fname : path.getParts()) {
            f = new File(f, fname);
        }
        return f;
    }


}
