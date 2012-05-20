/*
 * Copyright (C) 2012 McEvoy Software Ltd
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.spliffy.sync;

import com.ettrema.event.EventManager;
import com.ettrema.event.EventManagerImpl;
import com.ettrema.httpclient.Host;
import java.io.File;
import java.net.URL;

/**
 * Allows SpliffySync to be run from the command line
 *
 * @author brad
 */
public class SyncCommand {
    public static void main(String[] args) throws Exception {
        String sLocalDir = args[0];
        String sRemoteAddress = args[1];
        String user = args[2];
        String pwd = args[3];

        File localRootDir = new File(sLocalDir);
        URL url = new URL(sRemoteAddress);
        //HttpClient client = createHost(url, user, pwd);

        Host client = new Host(url.getHost(), url.getPort(), user, pwd, null);
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
        EventManager eventManager = new EventManagerImpl();
        Syncer syncer = new Syncer(eventManager, localRootDir, httpHashStore, httpBlobStore, client, archiver, url.getPath());

        SpliffySync spliffySync = new SpliffySync(localRootDir, client, url.getPath(), syncer, archiver, dbInit, eventManager);
        spliffySync.scan();

        System.out.println("Stats---------");
        System.out.println("fanouts cache: hits: " + fanoutsHashCache.getHits() + " misses:" + fanoutsHashCache.getMisses() + " inserts: " + fanoutsHashCache.getInserts());
        System.out.println("blobs cache: hits: " + blobsHashCache.getHits() + " misses:" + blobsHashCache.getMisses() + " inserts: " + blobsHashCache.getInserts());
        System.out.println("http hash gets: " + httpHashStore.getGets() + " sets: " + httpHashStore.getSets());
        System.out.println("http blob gets: " + httpBlobStore.getGets() + " sets: " + httpBlobStore.getSets());
    }    
}
