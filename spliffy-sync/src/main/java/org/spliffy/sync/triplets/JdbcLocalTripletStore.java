package org.spliffy.sync.triplets;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.utils.With;
import com.ettrema.db.Table;
import com.ettrema.db.TableCreatorService;
import com.ettrema.db.TableDefinitionSource;
import com.ettrema.db.UseConnection;
import com.ettrema.db.dialects.Dialect;
import com.ettrema.db.types.FieldTypes;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.hashsplit4j.api.BlobStore;
import org.hashsplit4j.api.FileBlobStore;
import org.hashsplit4j.api.NullHashStore;
import org.hashsplit4j.api.Parser;
import org.spliffy.common.Triplet;

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

    public static final CrcTable CRC_TABLE = new CrcTable();
    public static final BlobTable BLOB_TABLE = new BlobTable();
    private static ThreadLocal<Connection> tlConnection = new ThreadLocal<>();

    private static Connection con() {
        return tlConnection.get();
    }
    private final UseConnection useConnection;
    private final File root;
    private File currentScanFile;
    private long currentOffset;
    private long lastBlobHash;
    private byte[] lastBlob;

    /**
     *
     * @param useConnection
     * @param dialect
     * @param group - so we can cache different collections in one table
     */
    public JdbcLocalTripletStore(UseConnection useConnection, Dialect dialect, File root) {
        this.useConnection = useConnection;
        this.root = root;
        TableDefinitionSource defs = new TableDefinitionSource() {

            @Override
            public List<? extends Table> getTableDefinitions() {
                return Arrays.asList(CRC_TABLE, BLOB_TABLE);
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
        final File f = toFile(path);
        List<CrcRecord> records = useConnection.use(new With<Connection, List<CrcRecord>>() {

            @Override
            public List<CrcRecord> use(Connection con) throws Exception {
                List<CrcRecord> list = listCrcRecords(con, f.getAbsolutePath());
                return list;
            }
        });
        return toTriplets(f, records);
    }

    private List<Triplet> toTriplets(File parent, List<CrcRecord> records) {
        List<Triplet> list = new ArrayList<>();
        for (CrcRecord r : records) {
            File child = new File(parent, r.name);
            if (!child.exists()) {
                // cached information is out of date
                // TODO: should regenerate triplets, but should rarely happen
                throw new RuntimeException("Stale triplet information");
            }
            Triplet t = new Triplet();
            t.setHash(r.crc);
            t.setName(r.name);
            t.setType(toType(child));
        }
        return list;
    }

    /**
     * Must be inside a connection
     *
     * @param hash
     * @param bytes
     */
    @Override
    public void setBlob(long hash, byte[] bytes) {
        String sql = BLOB_TABLE.getInsert();
        try {
            try (PreparedStatement stmt = con().prepareStatement(sql)) {
                BLOB_TABLE.path.set(stmt, 1, currentScanFile.getAbsolutePath());
                BLOB_TABLE.crc.set(stmt, 2, hash);
                BLOB_TABLE.offset.set(stmt, 3, currentOffset);
                BLOB_TABLE.length.set(stmt, 4, bytes.length);
                BLOB_TABLE.date.set(stmt, 4, new Timestamp(System.currentTimeMillis()));
                stmt.execute();
            }
            currentOffset += bytes.length;
        } catch (SQLException ex) {
            throw new RuntimeException(sql, ex);
        }
    }

    @Override
    public byte[] getBlob(long hash) {
        try {
            if (hash == lastBlobHash) {
                return lastBlob;
            }
            List<BlobVector> list = listBlobsByHash(con(), hash);
            if (list.isEmpty()) {
                return null;
            }
            for (BlobVector v : list) {
                try {
                    byte[] blob = loadAndVerify(v);
                    if (blob != null) {
                        lastBlobHash = hash;
                        lastBlob = blob;
                        return blob;
                    }
                } catch (IOException e) {
                    System.out.println("couldnt load from vector: " + v);
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
                tlConnection.set(t);
                scan(root);
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
    private boolean scan(File dir) throws SQLException {
        File[] children = dir.listFiles();
        if (children == null) {
            return false;
        }
        boolean changed = false;
        for (File child : children) {
            if (child.isDirectory()) {
                if (scan(child)) {
                    changed = true;
                }
            }
        }
        if (scanChildren(dir)) {
            changed = true;
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
    private boolean scanChildren(final File parent) {
        final Map<String, File> mapOfFiles = toMap(parent.listFiles());

        Boolean didChange = useConnection.use(new With<Connection, Boolean>() {

            @Override
            public Boolean use(Connection c) throws Exception {
                Boolean changed = Boolean.FALSE;
                List<CrcRecord> oldRecords = listCrcRecords(c, parent.getAbsolutePath());
                Map<String, CrcRecord> mapOfRecords = toMap(oldRecords);

                // remove any that no longer exist
                for (CrcRecord r : oldRecords) {
                    if (!mapOfFiles.containsKey(r.name)) {
                        deleteCrc(c, parent.getAbsolutePath(), r.name);
                    }
                }

                for (File f : mapOfFiles.values()) {
                    CrcRecord r = mapOfRecords.get(f.getName());
                    if (r == null) {
                        changed = Boolean.TRUE;
                        scanFile(c, f);
                    } else {
                        if (r.date.getTime() != f.lastModified()) {
                            changed = Boolean.TRUE;
                            deleteCrc(c, parent.getAbsolutePath(), f.getName());
                            scanFile(c, f);
                        } else {
                            // cache is up to date
                        }
                    }
                }
                return changed;
            }
        });
        return didChange;
    }

    /**
     * Scan the file and generate records
     *
     * @param c
     * @param f
     */
    private void scanFile(Connection c, File f) throws FileNotFoundException, IOException, SQLException {
        this.currentScanFile = f; // will be used by setBlob
        this.currentOffset = 0;
        long crc = Parser.parse(f, this, new NullHashStore()); // will generate blobs into this blob store
        this.currentScanFile = null;
        insertCrc(c, f.getParent(), f.getName(), crc);
    }

    private Map<String, CrcRecord> toMap(List<CrcRecord> records) {
        Map<String, CrcRecord> map = new HashMap<>();
        for (CrcRecord r : records) {
            map.put(r.name, r);
        }
        return map;
    }

    private Map<String, File> toMap(File[] files) {
        Map<String, File> map = new HashMap<>();
        if (files != null) {
            for (File r : files) {
                map.put(r.getName(), r);
            }
        }
        return map;
    }

    private void deleteCrc(Connection c, String path, String name) throws SQLException {
        String sql = CRC_TABLE.getDelete() + " WHERE " + CRC_TABLE.path.getName() + " = ?" + " AND " + CRC_TABLE.name.getName() + " = ?";
        try (PreparedStatement stmt = c.prepareStatement(sql)) {
            CRC_TABLE.path.set(stmt, 1, path);
            CRC_TABLE.name.set(stmt, 2, name);
            stmt.execute();
        }
    }

    private void insertCrc(Connection c, String path, String name, long crc) throws SQLException {
        String sql = CRC_TABLE.getInsert();
        try (PreparedStatement stmt = c.prepareStatement(sql)) {
            CRC_TABLE.crc.set(stmt, 1, crc);
            CRC_TABLE.path.set(stmt, 2, path);
            CRC_TABLE.name.set(stmt, 3, name);
            CRC_TABLE.date.set(stmt, 3, new Timestamp(System.currentTimeMillis()));
            stmt.execute();
        }
    }

    private List<CrcRecord> listCrcRecords(Connection c, String path) throws SQLException {
        final String q = CRC_TABLE.getSelect() + " WHERE " + CRC_TABLE.path.getName() + " = ?";
        List<CrcRecord> oldRecords = new ArrayList<>();
        try (PreparedStatement stmt = c.prepareStatement(q)) {
            CRC_TABLE.path.set(stmt, 1, path);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    long crc = CRC_TABLE.crc.get(rs);
                    String name = CRC_TABLE.name.get(rs);
                    Timestamp date = CRC_TABLE.date.get(rs);
                    CrcRecord r = new CrcRecord(crc, name, date);
                    oldRecords.add(r);
                }
            }
        }
        return oldRecords;
    }

    private List<BlobVector> listBlobsByHash(Connection c, long hash) throws SQLException {
        final String q = BLOB_TABLE.getSelect() + " WHERE " + BLOB_TABLE.crc.getName() + " = ?";
        List<BlobVector> blobVectors = new ArrayList<>();
        try (PreparedStatement stmt = c.prepareStatement(q)) {
            CRC_TABLE.crc.set(stmt, 1, hash);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    long crc = BLOB_TABLE.crc.get(rs);
                    String path = BLOB_TABLE.path.get(rs);
                    Timestamp date = BLOB_TABLE.date.get(rs);
                    long offset = BLOB_TABLE.offset.get(rs);
                    int length = BLOB_TABLE.length.get(rs);
                    BlobVector r = new BlobVector(path, crc, offset, length, date);
                    blobVectors.add(r);
                }
            }
        }
        return blobVectors;
    }

    private File toFile(Path path) {
        File f = root;
        for (String fname : path.getParts()) {
            f = new File(f, fname);
        }
        return f;
    }

    private String toType(File child) {
        return child.isDirectory() ? "d" : "f";
    }

    private byte[] loadAndVerify(BlobVector v) throws FileNotFoundException, IOException {
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(currentScanFile, "r");
            return FileBlobStore.readBytes(raf, v.offset, v.length, v.crc); // implicitly verifies against given crc, will throw IOException if not valid
        } finally {
            IOUtils.closeQuietly(raf);
        }
    }

    public static class CrcTable extends com.ettrema.db.Table {

        public final Table.Field<Long> crc = add("crc", FieldTypes.LONG, false); // use "crc" instead of "hash" because hash is a reserved word
        public final Table.Field<String> path = add("path", FieldTypes.CHARACTER_VARYING, false);
        public final Table.Field<String> name = add("name", FieldTypes.CHARACTER_VARYING, false);
        public final Table.Field<java.sql.Timestamp> date = add("date_verified", FieldTypes.TIMESTAMP, false);

        public CrcTable() {
            super("file_crcs");
        }
    }

    public static class BlobTable extends com.ettrema.db.Table {

        public final Table.Field<String> path = add("path", FieldTypes.CHARACTER_VARYING, false);
        public final Table.Field<Long> crc = add("crc", FieldTypes.LONG, false);
        public final Table.Field<Long> offset = add("offset", FieldTypes.LONG, false);
        public final Table.Field<Integer> length = add("length", FieldTypes.INTEGER, false);
        public final Table.Field<java.sql.Timestamp> date = add("date_verified", FieldTypes.TIMESTAMP, false);

        public BlobTable() {
            super("file_blobs");
        }
    }

    private class CrcRecord {

        long crc;
        String name;
        Timestamp date;

        CrcRecord(long crc, String name, Timestamp date) {
            this.crc = crc;
            this.name = name;
            this.date = date;
        }
    }

    /**
     * Specifies where to find a blob, ie what file its in, at the given offset
     * and of the given length
     *
     */
    private class BlobVector {

        final String path;
        final long crc;
        final long offset;
        final int length;
        final Timestamp date;

        public BlobVector(String path, long crc, long offset, int length, Timestamp date) {
            this.path = path;
            this.crc = crc;
            this.offset = offset;
            this.length = length;
            this.date = date;
        }

        @Override
        public String toString() {
            return "BlobVector: " + path + "/" + offset + "/" + length;
        }
        
        
    }
}
