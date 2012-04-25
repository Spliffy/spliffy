package org.spliffy.server.web;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.zip.Adler32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.Checksum;
import org.apache.commons.io.output.NullOutputStream;
import org.hibernate.Session;
import org.spliffy.common.HashUtils;
import org.spliffy.server.db.DirEntry;
import org.spliffy.server.db.ResourceMeta;
import org.spliffy.server.db.ResourceVersionMeta;

/**
 *
 * @author brad
 */
public class HashCalc {

    public static long calcHash(List<DirEntry> childDirEntries) {
        OutputStream nulOut = new NullOutputStream();
        CheckedOutputStream cout = new CheckedOutputStream(nulOut, new Adler32());
        Set<String> names = new HashSet<>();
        for (DirEntry r : childDirEntries) {
            String name = r.getName();
            if (names.contains(name)) {
                throw new RuntimeException("Name not unique within collection: " + name);
            }
            names.add(name);
            ResourceVersionMeta metaV = ResourceVersionMeta.find(r.getMetaId());
            ResourceMeta meta = metaV.getResourceMeta();
            String line = HashUtils.toHashableText(name, r.getEntryHash(), r.getMetaId(), meta.getType());
            HashUtils.appendLine(line, cout);
        }
        Checksum check = cout.getChecksum();
        long crc = check.getValue();
        return crc;
    }

    public static long calcResourceesHash(List<MutableResource> children) {
        OutputStream nulOut = new NullOutputStream();
        try {
            return calcResourceesHash(children, nulOut);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Calculate the directory hash, outputting hashed text to the given stream
     *
     * @param children
     * @param out
     * @return
     */
    public static long calcResourceesHash(List<MutableResource> children, OutputStream out) throws IOException {
        CheckedOutputStream cout = new CheckedOutputStream(out, new Adler32());
        for (MutableResource r : children) {
            String type = (r instanceof MutableCollection) ? "d" : "f";
            String line = HashUtils.toHashableText(r.getName(), r.getEntryHash(), r.getMetaId(), type);
            HashUtils.appendLine(line, cout);
        }
        cout.flush();
        Checksum check = cout.getChecksum();
        long crc = check.getValue();
        return crc;
    }

    /**
     * Copy DirEntry records to point to new dirHash
     *
     * @param dirHash
     * @param children
     */
    static void saveKids(Session session, long dirHash, List<MutableResource> children) {
        // First check, this dirHash might already be in the database. If so we don't do anything
        // Note that the dirHash defines all of the child entries, so if it exists at all
        // then we do not need to create any child entries
        List<DirEntry> existing = DirEntry.listEntries(session, dirHash);
        if( existing != null && !existing.isEmpty()) {
            System.out.println("dirHash already exists");
            return ;
        }
        for (MutableResource r : children) {
            DirEntry entry = new DirEntry();
            entry.setId(UUID.randomUUID()); // todo: really don't need id's for this
            entry.setParentHash(dirHash);
            entry.setEntryHash(r.getEntryHash());
            entry.setName(r.getName());
            entry.setMetaId(r.getMetaId());
            try {
                session.save(entry);
                session.flush();
            } catch (Throwable e) {
                System.out.println("Error inserting: " + dirHash + " - " + r.getName());
                throw new RuntimeException(e);
            }
        }
    }
}
