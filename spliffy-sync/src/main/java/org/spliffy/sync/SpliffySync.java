package org.spliffy.sync;

import org.spliffy.sync.triplets.JdbcLocalTripletStore;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.exceptions.NotFoundException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.hashsplit4j.api.HttpBlobStore;
import org.hashsplit4j.api.HttpHashStore;
import org.spliffy.sync.triplets.HttpTripletStore;

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

        File localRootDir = new File(sLocalDir);
        URL url = new URL(sRemoteAddress);
        HttpClient client = createHost(url, user, pwd);

        System.out.println("Sync: " + localRootDir.getAbsolutePath() + " - " + sRemoteAddress);

        File dbFile = new File("target/sync-db");
        System.out.println("Using database: " + dbFile.getAbsolutePath());

        DbInitialiser dbInit = new DbInitialiser(dbFile);

        JdbcHashCache fanoutsHashCache = new JdbcHashCache(dbInit.getUseConnection(), dbInit.getDialect(), "h");
        JdbcHashCache blobsHashCache = new JdbcHashCache(dbInit.getUseConnection(), dbInit.getDialect(), "b");

        HttpHashStore httpHashStore = new HttpHashStore(client, fanoutsHashCache);
        httpHashStore.setBaseUrl("/_hashes/fanouts/");
        HttpBlobStore httpBlobStore = new HttpBlobStore(client, blobsHashCache);
        httpBlobStore.setBaseUrl("/_hashes/blobs/");

        Archiver archiver = new Archiver();
        Syncer syncer = new Syncer(localRootDir, httpHashStore, httpBlobStore, client, archiver, url.getPath());

        SpliffySync spliffySync = new SpliffySync(localRootDir, client, url.getPath(), syncer, archiver, dbInit);
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
    private final File localRoot;
    private final DbInitialiser dbInit;
    private final HttpClient httpClient;
    private final Syncer syncer;
    private final String basePath;
    private final Archiver archiver;

    public SpliffySync(File local, HttpClient httpClient, String basePath, Syncer syncer, Archiver archiver, DbInitialiser dbInit) {
        this.localRoot = local;
        this.httpClient = httpClient;
        this.basePath = basePath;
        this.syncer = syncer;
        this.archiver = archiver;
        this.dbInit = dbInit;                
    }

    public void scan() throws com.ettrema.httpclient.HttpException, NotAuthorizedException, BadRequestException, ConflictException, NotFoundException, IOException {
        doScan();
    }

    public void doScan() throws IOException, com.ettrema.httpclient.HttpException, NotAuthorizedException, BadRequestException, ConflictException, NotFoundException {
        HttpTripletStore remoteTripletStore = new HttpTripletStore(httpClient, basePath);
        JdbcLocalTripletStore jdbcTripletStore = new JdbcLocalTripletStore(dbInit.getUseConnection(), dbInit.getDialect(), localRoot);
        JdbcSyncStatusStore statusStore = new JdbcSyncStatusStore(dbInit.getUseConnection(), dbInit.getDialect(), basePath, localRoot);
        DeltaListener2 deltaListener2 = new SyncingDeltaListener(syncer, archiver, localRoot, statusStore);
        
        DirWalker dirWalker = new DirWalker(remoteTripletStore, jdbcTripletStore, statusStore, deltaListener2);
                
        // Now do the 
        dirWalker.walk();
    }
}
