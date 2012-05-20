package org.spliffy.sync;

import com.bradmcevoy.common.Path;
import org.spliffy.sync.triplets.JdbcLocalTripletStore;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.exceptions.NotFoundException;
import com.ettrema.event.Event;
import com.ettrema.event.EventListener;
import com.ettrema.event.EventManager;
import com.ettrema.event.EventManagerImpl;
import com.ettrema.httpclient.Host;
import com.ettrema.httpclient.HttpException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.*;
import org.spliffy.sync.event.FileChangedEvent;
import org.spliffy.sync.triplets.HttpTripletStore;

/**
 *
 * @author brad
 */
public class SpliffySync {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SpliffySync.class);

    private final File localRoot;
    private final DbInitialiser dbInit;
    private final Host httpClient;
    private final Syncer syncer;
    private final String basePath;
    private final Archiver archiver;
    private final HttpTripletStore remoteTripletStore;
    private final JdbcLocalTripletStore jdbcTripletStore;
    private final JdbcSyncStatusStore statusStore;
    private final DeltaListener2 deltaListener2;
    private final EventManager eventManager;
    private ScheduledExecutorService scheduledExecService;
    private ScheduledFuture<?> scanJob;
    private boolean paused;

    public SpliffySync(File local, Host httpClient, String basePath, Syncer syncer, Archiver archiver, DbInitialiser dbInit, EventManager eventManager) throws IOException {
        this.localRoot = local;
        this.httpClient = httpClient;
        this.basePath = basePath;
        this.syncer = syncer;
        this.archiver = archiver;
        this.dbInit = dbInit;
        this.eventManager = eventManager;
        remoteTripletStore = new HttpTripletStore(httpClient, Path.path(basePath));
        jdbcTripletStore = new JdbcLocalTripletStore(dbInit.getUseConnection(), dbInit.getDialect(), localRoot, eventManager);
        statusStore = new JdbcSyncStatusStore(dbInit.getUseConnection(), dbInit.getDialect(), basePath, localRoot);
        deltaListener2 = new SyncingDeltaListener(syncer, archiver, localRoot, statusStore);       
    }

    /**
     * Perform an immediate scan
     * 
     * @throws com.ettrema.httpclient.HttpException
     * @throws NotAuthorizedException
     * @throws BadRequestException
     * @throws ConflictException
     * @throws NotFoundException
     * @throws IOException 
     */
    public void scan() throws com.ettrema.httpclient.HttpException, NotAuthorizedException, BadRequestException, ConflictException, NotFoundException, IOException {
        DirWalker dirWalker = new DirWalker(remoteTripletStore, jdbcTripletStore, statusStore, deltaListener2);

        // Now do the 
        dirWalker.walk();
    }

    /**
     * Start the syncronisation service. This will schedule the first scan after 
     * a short delay, then will scan at intervals after that.
     * 
     */
    public void start() {
        // subscribe to receive notifications when the file system changes
        eventManager.registerEventListener(new SpliffySyncEventListener(), FileChangedEvent.class);
        scheduledExecService = Executors.newScheduledThreadPool(1);
        // schedule a job to do the scanning with a fixed interval
        scanJob = scheduledExecService.scheduleWithFixedDelay(new ScanRunner(),5000, 60000, TimeUnit.MILLISECONDS);
        jdbcTripletStore.start();
    }

    public void stop() {
        if (scanJob != null) {
            scanJob.cancel(true);
            scanJob = null;
        }
        if (scheduledExecService != null) {
            scheduledExecService.shutdownNow();
            scheduledExecService = null;
        }
        jdbcTripletStore.stop();
    }
    
    public void setPaused(boolean state) {
        paused = state;
        syncer.setPaused(state);
    }
    
    public boolean isPaused() {
        return paused;
    }

    private class ScanRunner implements Runnable {

        @Override
        public void run() {
            log.info("doing scan..");
            try {
                if( !paused ) {
                    scan();
                }
            } catch (HttpException | NotAuthorizedException | BadRequestException | ConflictException | NotFoundException | IOException ex) {
                log.error("Exception during scan", ex);
            }
        }
    }

    private class SpliffySyncEventListener implements EventListener {

        @Override
        public void onEvent(Event e) {
            if (e instanceof FileChangedEvent) {
                log.info("File changed event, doing scan..");
                if( !paused ) {
                    scheduledExecService.submit(new ScanRunner());
                }
            }
        }
    }
}
