package org.spliffy.sync.triplets;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.utils.With;
import com.ettrema.db.Table;
import com.ettrema.db.TableCreatorService;
import com.ettrema.db.TableDefinitionSource;
import com.ettrema.db.UseConnection;
import com.ettrema.db.dialects.Dialect;
import java.io.*;
import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.hashsplit4j.api.BlobStore;
import org.hashsplit4j.api.NullHashStore;
import org.hashsplit4j.api.Parser;
import org.spliffy.common.HashUtils;
import org.spliffy.common.Triplet;
import org.spliffy.sync.Utils;
import org.spliffy.sync.triplets.BlobDao.BlobVector;
import org.spliffy.sync.triplets.CrcDao.CrcRecord;

/**
 * Scans the given root directory on startup to ensure the triplet table is up
 * to date.
 *
 * Once scanning is complete it acts as a TripletStore
 *
 * Does a depth-first scan of the directory. For each file it checks if the file
 * is present in the table and if the modified date is unchanged
 *
 * If anything was changed after scanning the children of a directory then the
 * directories own hash is updated (which is why it must be depth first)
 *
 * As part of scanning files this will populate a blobs table containing the
 * hash , offset and length of the blob, allowing this to be used as a blob
 * source
 *
 * @author brad
 */
public class JdbcLocalTripletStore implements TripletStore, BlobStore {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(JdbcLocalTripletStore.class);
    
    private static ThreadLocal<Connection> tlConnection = new ThreadLocal<>();

    private static Connection con() {
        return tlConnection.get();
    }
    private final UseConnection useConnection;
    private final CrcDao crcDao;
    private final BlobDao blobDao;
    private final File root;
    private File currentScanFile;
    private long currentOffset;
    private long lastBlobHash;
    private byte[] lastBlob;
    private boolean initialScanDone;

    /**
     *
     * @param useConnection
     * @param dialect
     * @param group - so we can cache different collections in one table
     */
    public JdbcLocalTripletStore(UseConnection useConnection, Dialect dialect, File root) {
        this.useConnection = useConnection;
        this.root = root;
        this.crcDao = new CrcDao();
        this.blobDao = new BlobDao();
        TableDefinitionSource defs = new TableDefinitionSource() {

            @Override
            public List<? extends Table> getTableDefinitions() {
                return Arrays.asList(CrcDao.CRC, BlobDao.BLOB);
            }

            @Override
            public void onCreate(Table t, Connection con) {
            }
        };
        final TableCreatorService creatorService = new TableCreatorService(null, Arrays.asList(defs), dialect);

        useConnection.use(new With<Connection, Object>() {

            @Override
            public Object use(Connection con) throws Exception {
                creatorService.processTableDefinitions(con);
                return null;
            }
        });
    }

    @Override
    public List<Triplet> getTriplets(Path path) {
        if (!initialScanDone) {
            System.out.println("Initial scan not done, doing it now...");
            scan();
            initialScanDone = true;
            System.out.println("Initial scan finished. Now, where were we...");

        }

        final File f = Utils.toFile(root, path);        
        List<CrcRecord> records = useConnection.use(new With<Connection, List<CrcRecord>>() {

            @Override
            public List<CrcRecord> use(Connection con) throws Exception {
                tlConnection.set(con);                                
                List<CrcRecord> list = crcDao.listCrcRecords(con, f.getAbsolutePath());
                log.trace("crc records: " + list.size() + " - " + f.getAbsolutePath());
                tlConnection.remove();
                return list;
            }
        });
        log.trace("JdbcLocalTripletStore: getTriplets: " + f.getAbsolutePath() + " - " + records.size());
        return BlobUtils.toTriplets(f, records);
    }

    /**
     * Must be inside a connection
     *
     * @param hash
     * @param bytes
     */
    @Override
    public void setBlob(long hash, byte[] bytes) {
        blobDao.insertBlob(hash, bytes, currentScanFile.getAbsolutePath(), currentOffset, con());
        currentOffset += bytes.length;
    }

    @Override
    public byte[] getBlob(long hash) {
        try {
            if (hash == lastBlobHash) {  // this will often happen because hasBlob will be called first for same hash
                return lastBlob;
            }
            List<BlobVector> list = blobDao.listBlobsByHash(con(), hash);

            for (BlobVector v : list) {
                try {
                    byte[] blob = BlobUtils.loadAndVerify(currentScanFile, v);
                    if (blob != null) {
                        lastBlobHash = hash;
                        lastBlob = blob;
                    }
                    return blob;
                } catch (IOException e) {
                    System.out.println("couldnt load from vector: " + v + "  probably no longer valid so will delete the blob record");
                    blobDao.deleteBlob(v.path, v.crc, v.offset, con());
                }
            }
            return null;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public boolean hasBlob(long hash) {
        return getBlob(hash) != null;
    }

    public void scan() {
        useConnection.use(new With<Connection, Object>() {

            @Override
            public Object use(Connection t) throws Exception {
                System.out.println("START SCAN");
                tlConnection.set(t);
                scanDirectory(root);
                con().commit();

                long count = crcDao.getCrcRecordCount(con());
                System.out.println("Contains crc records: " + count);
                
                
                tlConnection.remove();
                return null;
            }
        });
    }

    /**
     *
     * @param dir
     * @return - true if anything was changed
     */
    private boolean scanDirectory(File dir) throws SQLException, IOException {
        if (Utils.ignored(dir)) {
            return false;
        }

        File[] children = dir.listFiles();
        if (children == null) {
            log.trace("No children of: " + dir.getAbsolutePath());
            return false;
        }
        boolean changed = false;
        for (File child : children) {
            if (child.isDirectory()) {
                if (scanDirectory(child)) {
                    changed = true;
                }
            }
        }
        if (scanChildren(dir)) {
            changed = true;
        }

        if (changed) {
            log.info("changed records found, refresh diretory record: " + dir.getAbsolutePath());
            generateDirectoryRecord(con(), dir); // insert/update the hash for this directory
        }

        con().commit(); // commit every now and then
        return changed;
    }

    /**
     * Get all the records for this directory and compare them with current
     * files. Delete records with no corresponding file/directory, insert new
     * ones, and update if the recorded modified date differs from actual
     *
     * @param parent
     * @return - true if anything has changed
     */
    private boolean scanChildren(final File parent) throws SQLException, IOException {
        final Map<String, File> mapOfFiles = Utils.toMap(parent.listFiles());

        boolean changed = false;
        List<CrcRecord> oldRecords = crcDao.listCrcRecords(con(), parent.getAbsolutePath());
        Map<String, CrcRecord> mapOfRecords = CrcDao.toMap(oldRecords);

        // remove any that no longer exist
        for (CrcRecord r : oldRecords) {
            if (!mapOfFiles.containsKey(r.name)) {
                changed = Boolean.TRUE;
                File fRemoved = new File(parent, r.name);
                log.trace("detected change, file removed: " + fRemoved.getAbsolutePath());
                crcDao.deleteCrc(con(), parent.getAbsolutePath(), r.name);
            }
        }

        for (File f : mapOfFiles.values()) {
            CrcRecord r = mapOfRecords.get(f.getName());
            if (r == null) {
                log.trace("detected change, new file: " + f.getAbsolutePath() + " in map of size: " + mapOfRecords.size());
                changed = Boolean.TRUE;
                scanFile(con(), f);
            } else {
                if (r.date.getTime() != f.lastModified()) {
                    log.trace("detected change, file modified dates differ: " + f.getAbsolutePath());
                    changed = Boolean.TRUE;
                    crcDao.deleteCrc(con(), parent.getAbsolutePath(), f.getName());
                    scanFile(con(), f);
                } else {
                    // cache is up to date
                }
            }
        }

        return changed;
    }

    /**
     * Scan the file and generate records
     *
     * @param c
     * @param f
     */
    private void scanFile(Connection c, File f) throws IOException, SQLException {
        if (f.isDirectory()) {
            return; // will generate directory records in scan after all children are processed
        }
        this.currentScanFile = f; // will be used by setBlob
        this.currentOffset = 0;
        long crc = Parser.parse(f, this, new NullHashStore()); // will generate blobs into this blob store
        this.currentScanFile = null;
        crcDao.insertCrc(c, f.getParent(), f.getName(), crc, f.lastModified());
    }

    /**
     * Called after all children of the directory have been processed, and only
     * if a change was detected in a child
     *
     * @param dir
     */
    private void generateDirectoryRecord(Connection c, File dir) throws SQLException {
        crcDao.deleteCrc(con(), dir.getParent(), dir.getName());
        // Note that we're reloading triplets, strictly not necessary but is a bit safer then
        // reusing the list we've been changing

        List<CrcRecord> crcRecords = crcDao.listCrcRecords(con(), dir.getAbsolutePath());
        List<Triplet> triplets = BlobUtils.toTriplets(dir, crcRecords);
        long newHash = HashUtils.calcTreeHash(triplets);
        log.info("Insert new directory hash: " + dir.getParent() + " :: " + dir.getName() + " = " + newHash);
        crcDao.insertCrc(c, dir.getParent(), dir.getName(), newHash, dir.lastModified());
    }
}
