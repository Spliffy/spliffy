package org.spliffy.sync;

import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.exceptions.NotFoundException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.JOptionPane;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.hashsplit4j.api.HashCache;
import org.hashsplit4j.api.HttpBlobStore;
import org.hashsplit4j.api.HttpHashStore;

/**
 *
 * @author brad
 */
public class SpliffySync {
    public static void main(String[] args) throws Exception {
        String sLocalDir = args[0];
        String sRemoteAddress = args[1];
        String user = args[2];
        String pwd = args[3];
        
        File fLocal = new File(sLocalDir);
        URL url = new URL(sRemoteAddress);
        HttpClient client = createHost(url, user, pwd);
        
        System.out.println("Sync: " + fLocal.getAbsolutePath() + " - " + sRemoteAddress);
        
        File dbFile = new File("target/db");
        System.out.println("Using database: " + dbFile.getAbsolutePath());
        
        DbInitialiser dbInit = new DbInitialiser(dbFile);
        
        JdbcHashCache fanoutsHashCache = new JdbcHashCache(dbInit.getUseConnection(), dbInit.getDialect(), "h");
        JdbcHashCache blobsHashCache = new JdbcHashCache(dbInit.getUseConnection(), dbInit.getDialect(), "b");
        
        HttpHashStore httpHashStore = new HttpHashStore(client, fanoutsHashCache);        
        httpHashStore.setBaseUrl( "/_hashes/fanouts/");
        HttpBlobStore httpBlobStore = new HttpBlobStore(client, blobsHashCache);
        httpBlobStore.setBaseUrl( "/_hashes/blobs/");
                
        LastBackedupStore lastBackedupStore = new JdbcLastBackedupStore(dbInit.getUseConnection(), dbInit.getDialect());
        DeltaWalker deltaWalker = new DefaultDeltaWalker(client, lastBackedupStore);
        
        Syncer syncer = new Syncer(httpHashStore, httpBlobStore, lastBackedupStore, client, new Archiver());
        syncer.setBaseUrl(url.getPath());
                
        SpliffySync spliffySync = new SpliffySync(fLocal, client, lastBackedupStore, url.getPath(), deltaWalker, syncer);
        spliffySync.scan();        
        
        System.out.println("Stats---------");
        System.out.println("fanouts cache: hits: " + fanoutsHashCache.getHits() + " misses:" + fanoutsHashCache.getMisses() + " inserts: " + fanoutsHashCache.getInserts());
        System.out.println("blobs cache: hits: " + blobsHashCache.getHits() + " misses:" + blobsHashCache.getMisses() + " inserts: " + blobsHashCache.getInserts());
        System.out.println("http hash gets: " + httpHashStore.getGets() + " sets: " + httpHashStore.getSets());
        System.out.println("http blob gets: " + httpBlobStore.getGets() + " sets: " + httpBlobStore.getSets());
    }

    private static HttpClient createHost(URL url, String user, String pwd) throws MalformedURLException {        
        
        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams().setConnectionTimeout(10000);
        if (user != null) {
            client.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(user, pwd));
        }

        if (user != null && user.length() > 0) {
            client.getParams().setAuthenticationPreemptive(true);
        }
        client.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
        client.getParams().setSoTimeout(30000);
        client.getParams().setConnectionManagerTimeout(30000);
        HttpMethodRetryHandler handler = new DefaultHttpMethodRetryHandler(0, false); // no retries
        client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, handler);
        client.getHostConfiguration().setHost(url.getHost(), url.getPort(), url.getProtocol());
        return client;
    }    
    
    private final File local;
    private final HttpClient httpClient;
    private final Syncer syncer;
    private final String basePath;
    private final DeltaWalker deltaWalker;

    private long localHash;
    private Long remoteHash;
    
    public SpliffySync(File local, HttpClient httpClient, LastBackedupStore hashCache, String basePath, DeltaWalker deltaWalker, Syncer syncer) {
        this.local = local;
        this.httpClient = httpClient;
        this.basePath = basePath;
        this.deltaWalker = deltaWalker;                      
        this.syncer = syncer;
    }

    public void scan() throws com.ettrema.httpclient.HttpException, NotAuthorizedException, BadRequestException, ConflictException, NotFoundException, IOException {
        doScan();
    }
    
    public void doScan() throws IOException, com.ettrema.httpclient.HttpException, NotAuthorizedException, BadRequestException, ConflictException, NotFoundException {
        ScanningHashStore scanningHashStore = new ScanningHashStore(httpClient, local, basePath);
        localHash = scanningHashStore.scan();
        System.out.println("Local hash: " + localHash);
        System.out.println("Remote hash: " + remoteHash);
        if (remoteHash != null) {
            if (localHash == remoteHash.longValue()) {
                System.out.println("Directories are identical");
                return;
            }
        }

        SyncingDeltaListener deltaListener = new SyncingDeltaListener();
        deltaWalker.scanDir(local, basePath, scanningHashStore, deltaListener);              
    }

    private class SyncingDeltaListener implements DeltaListener {

        @Override
        public void onRemoteChange(long hash, File localChild) throws IOException {
            syncer.downloadSync(hash, localChild);
        }

        @Override
        public void onLocalChange(String encodedPath, File localFile) throws IOException {            
            // 
            if( localFile.isFile()) {
                System.out.println("LocalChange - " + localFile.getAbsolutePath());
                syncer.upSync(encodedPath, localFile);
            } else {
                // ignore locally new directories. Remote dirs get created implicitly
                // when files are uploaded
                // Probably should explicitly create empty dirs if needed though
                System.out.println("New directory, but ignoring for now");
            }
        }

        @Override
        public void onTreeConflict(File localChild) {
            Thread.dumpStack();
            JOptionPane.showMessageDialog(null, "Oh oh, remote is a file but local is a directory: " + localChild.getAbsolutePath());
        }

        @Override
        public void onFileConflict(long remoteHash, File localFile, String encodedPath) {
            Thread.dumpStack();
            JOptionPane.showMessageDialog(null, "Files are in conflict. There has been a change to a local file, but also a change to the corresponding remote file: " + localFile.getAbsolutePath());
        }

        @Override
        public void onLocallyDeleted(File localChild, String childEncodedPath) {
            syncer.deleteRemoteFile(childEncodedPath);
        }                
    }
    


}
