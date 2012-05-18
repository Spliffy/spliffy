package org.spliffy.sync;

import org.spliffy.sync.triplets.JdbcLocalTripletStore;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.exceptions.NotFoundException;
import com.ettrema.httpclient.Host;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.auth.*;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
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
        //HttpClient client = createHost(url, user, pwd);
        
        Host client = new Host(url.getHost(), url.getPath(), url.getPort(), user, pwd, null, null);
        boolean secure = url.getProtocol().equals("https");
        client.setSecure(secure);
        

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


    private final File localRoot;
    private final DbInitialiser dbInit;
    private final Host httpClient;
    private final Syncer syncer;
    private final String basePath;
    private final Archiver archiver;

    public SpliffySync(File local, Host httpClient, String basePath, Syncer syncer, Archiver archiver, DbInitialiser dbInit) {
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
    
    static class PreemptiveAuthInterceptor implements HttpRequestInterceptor {

        @Override
        public void process(final HttpRequest request, final HttpContext context) {
            AuthState authState = (AuthState) context.getAttribute(ClientContext.TARGET_AUTH_STATE);

            // If no auth scheme avaialble yet, try to initialize it
            // preemptively
            if (authState.getAuthScheme() == null) {
                AuthScheme authScheme = (AuthScheme) context.getAttribute("preemptive-auth");
                if (authScheme != null) {
                    CredentialsProvider credsProvider = (CredentialsProvider) context.getAttribute(ClientContext.CREDS_PROVIDER);
                    HttpHost targetHost = (HttpHost) context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
                    Credentials creds = credsProvider.getCredentials(new AuthScope(targetHost.getHostName(), targetHost.getPort()));
                    if (creds == null) {
                        throw new RuntimeException("No credentials for preemptive authentication");
                    }
                    authState.setAuthScheme(authScheme);
                    authState.setCredentials(creds);
                }
            }

        }
    }    
}
