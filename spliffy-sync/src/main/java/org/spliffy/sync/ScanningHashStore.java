package org.spliffy.sync;

import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.exceptions.NotFoundException;
import com.ettrema.httpclient.HttpException;
import java.io.*;
import java.util.*;
import java.util.zip.Adler32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.Checksum;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.io.output.NullOutputStream;
import org.hashsplit4j.api.*;
import org.spliffy.common.FileTriplet;
import org.spliffy.common.HashUtils;

/**
 *
 * @author brad
 */
public class ScanningHashStore implements HashStore {

    private final HttpClient httpClient;
    private final String basePath;
    private final File local;
    private FileTriplet rootTriplet = new FileTriplet();
    private Map<File, LocalFileTriplet> mapOfLocalTriplets = new HashMap<>();
    private final MemoryHashStore hashStore = new MemoryHashStore();

    public ScanningHashStore(HttpClient httpClient, File local, String baseUrl) {
        this.httpClient = httpClient;
        this.local = local;
        this.basePath = baseUrl;
    }

    public long scan() throws IOException, HttpException, NotAuthorizedException, BadRequestException, ConflictException, NotFoundException {
        System.out.println("ScanningHashStore: scan");
        String encodedPath = basePath;
        return walkTree(local, rootTriplet, encodedPath);
    }

    public LocalFileTriplet getLocalTriplet(File localFile) {
        return mapOfLocalTriplets.get(localFile);
    }

    /**
     *
     * @param dir - the local directory being scanned
     * @param dirTriplet - triplet representing the directory being scanner
     * @param encodedDirPath - the percentage encoded path of the remote
     * repository
     * @return - the hash of the directory being scanned
     * @throws IOException
     */
    private long walkTree(File dir, FileTriplet dirTriplet, String encodedDirPath) throws IOException, HttpException, NotAuthorizedException, BadRequestException, ConflictException {
        //      System.out.println("walkTree: " + encodedDirPath);
        // Need to load triplets from remote host to get meta id's
        System.out.println("walkTree: " + dir.getAbsolutePath());
        Map<String, FileTriplet> mapOfRemoteTriplets;
        try {
            byte[] arrRemoteTriplets = HttpUtils.get(httpClient, encodedDirPath + "?type=hashes");
            List<FileTriplet> triplets = HashUtils.parseTriplets(new ByteArrayInputStream(arrRemoteTriplets));
            mapOfRemoteTriplets = HashUtils.toMap(triplets);
        } catch (NotFoundException ex) {
            mapOfRemoteTriplets = new HashMap<>();
        }


        // Now scan local files
        List<File> files = orderedList(dir.listFiles());
        List<FileTriplet> fileTriplets = new ArrayList<>();
        for (File childFile : files) {
            if (!ignored(childFile)) {
                String name = childFile.getName();
                LocalFileTriplet childTriplet = new LocalFileTriplet(childFile);
                mapOfLocalTriplets.put(childFile, childTriplet);
                fileTriplets.add(childTriplet);
                childTriplet.setName(name);
                childTriplet.setType(childFile.isDirectory() ? "d" : "f");
                long hash;
                if (childFile.isDirectory()) {
                    String childPath = encodedDirPath + com.bradmcevoy.http.Utils.percentEncode(name) + "/";
                    hash = walkTree(childFile, childTriplet, childPath);
                } else {
                    hash = parseFile(childFile, childTriplet.getBlobStore());
                }
                childTriplet.setHash(hash);
                FileTriplet remoteTriplet = mapOfRemoteTriplets.get(name);
                if (remoteTriplet != null) {
//                System.out.println("got remote meta: " + remoteTriplet.getName() + " - " + remoteTriplet.getMetaId());
                    childTriplet.setMetaId(remoteTriplet.getMetaId());
                }
            }
        }
        long hash = calcTreeHash(fileTriplets);
        dirTriplet.setChildren(fileTriplets);
        return hash;
    }

    private List<File> orderedList(File[] listFiles) {
        List<File> list = new ArrayList<>();
        if (listFiles != null) {
            list.addAll(Arrays.asList(listFiles));
            Collections.sort(list);
        }
        return list;
    }

    private long parseFile(File childFile, BlobStore blobStore) throws IOException {
        Parser parser = new Parser();
        FileInputStream fin = new FileInputStream(childFile);
        long hash = parser.parse(fin, hashStore, blobStore);
        return hash;
    }

    private long calcTreeHash(List<FileTriplet> fileTriplets) {
        OutputStream nulOut = new NullOutputStream();
        CheckedOutputStream cout = new CheckedOutputStream(nulOut, new Adler32());
        Set<String> names = new HashSet<>();
        for (FileTriplet r : fileTriplets) {
            String name = r.getName();
            if (names.contains(name)) {
                throw new RuntimeException("Name not unique within collection: " + name);
            }
            names.add(name);
            String line = HashUtils.toHashableText(name, r.getHash(), r.getMetaId(), r.getType());
            HashUtils.appendLine(line, cout);
        }
        Checksum check = cout.getChecksum();
        long crc = check.getValue();
        return crc;
    }

    @Override
    public void setFanout(long hash, List<Long> childCrcs, long actualContentLength) {
        hashStore.setFanout(hash, childCrcs, actualContentLength);
    }

    @Override
    public Fanout getFanout(long fanoutHash) {
        return hashStore.getFanout(fanoutHash);
    }

    @Override
    public boolean hasFanout(long fanoutHash) {
        return hashStore.hasFanout(fanoutHash);
    }

    public boolean ignored(File childFile) {
        return childFile.isHidden() || childFile.getName().startsWith(".");
    }
}
