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
import org.spliffy.server.db.DirEntry;

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
            if( names.contains(name )) {
                throw new RuntimeException("Name not unique within collection: " + name);
            }
            names.add(name);                    
            String line = toHashableText(name, r.getEntryHash(), r.getMetaId());
            appendLine(line, cout);
        }
        Checksum check = cout.getChecksum();
        long crc = check.getValue();
        return crc;
    }

    public static long calcResourceesHash(List<AbstractSpliffyResource> children) {
        OutputStream nulOut = new NullOutputStream();
        try {
            return calcResourceesHash(children, nulOut);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * Calculate the directory hash, outputting hashed text to the given
     * stream
     * 
     * @param children
     * @param out
     * @return 
     */
    public static long calcResourceesHash(List<AbstractSpliffyResource> children, OutputStream out) throws IOException {        
        CheckedOutputStream cout = new CheckedOutputStream(out, new Adler32());
        for (AbstractSpliffyResource r : children) {
            String line = toHashableText(r.getName(), r.getEntryHash(), r.getMetaId() );
            appendLine(line, cout);
        }
        cout.flush();
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
        System.out.println("saveKids: parent hash: " + dirHash);
        for( AbstractSpliffyResource r : children ) {
            DirEntry entry = new DirEntry();
            entry.setId(UUID.randomUUID()); // todo: really don't need id's for this
            entry.setParentHash(dirHash);
            entry.setEntryHash(r.getEntryHash());
            entry.setName(r.getName());
            entry.setMetaId(r.getMetaId());
            System.out.println("    child: " + r.getName());
            session.save(entry);
        }
    }
}
