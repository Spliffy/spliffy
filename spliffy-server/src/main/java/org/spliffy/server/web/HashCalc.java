package org.spliffy.server.web;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;
import java.util.zip.Adler32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.Checksum;
import org.apache.commons.io.output.NullOutputStream;
import org.hibernate.Session;
import org.spliffy.server.db.DirEntry;

/**
 *
 * @author brad
 */
public class HashCalc {

    public static long calcHash(List<DirEntry> childDirEntries) {
        OutputStream nulOut = new NullOutputStream();
        CheckedOutputStream cout = new CheckedOutputStream(nulOut, new Adler32());
        for (DirEntry r : childDirEntries) {
            String line = toHashableText(r.getName(), r.getEntryHash(), r.getMetaId());
            appendLine(line, cout);
        }
        Checksum check = cout.getChecksum();
        long crc = check.getValue();
        return crc;
    }

    public static long calcResourceesHash(List<AbstractSpliffyResource> children) {
        OutputStream nulOut = new NullOutputStream();
        CheckedOutputStream cout = new CheckedOutputStream(nulOut, new Adler32());
        for (AbstractSpliffyResource r : children) {
            String line = toHashableText(r.getName(), r.getEntryHash(), r.getMetaId() );
            appendLine(line, cout);
        }
        Checksum check = cout.getChecksum();
        long crc = check.getValue();
        return crc;
    }
    
    /**
     * 
     * @param name - the name of the resource as it appears withint the current directory
     * @param crc - the hash of the resource. Either the crc of the file, of the hashed value of its members if a directory (ie calculated with this method)
     * @param type - "f" = file, "d" = directory
     * @return 
     */
    public static String toHashableText(String name, Long crc, UUID metaId) {
        String line = name + ":" + crc + ":" + metaId + '\n';
        return line;
    }

    public static void appendLine(String line, CheckedOutputStream cout) {
        if (line == null) {
            return;
        }
        try {
            cout.write(line.getBytes());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Copy DirEntry records to point to new dirHash
     * 
     * @param dirHash
     * @param children 
     */
    static void saveKids(Session session, long dirHash, List<AbstractSpliffyResource> children) {
        System.out.println("SaveKids: " + children.size());
        for( AbstractSpliffyResource r : children ) {
            DirEntry entry = new DirEntry();
            entry.setId(UUID.randomUUID()); // todo: really don't need id's for this
            entry.setParentHash(dirHash);
            entry.setEntryHash(r.getEntryHash());
            entry.setName(r.getName());
            entry.setMetaId(r.getMetaId());
            System.out.println("save: " + r.getName());
            session.save(entry);
        }
    }
}
