package org.spliffy.server.web;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.Adler32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.Checksum;
import org.apache.commons.io.output.NullOutputStream;
import org.spliffy.common.HashUtils;
import org.spliffy.server.db.DirectoryMember;
import org.spliffy.server.db.Item;
import org.spliffy.server.db.ItemVersion;

/**
 *
 * @author brad
 */
public class HashCalc {

    public static long calcHash(List<DirectoryMember> childDirEntries) {
        OutputStream nulOut = new NullOutputStream();
        CheckedOutputStream cout = new CheckedOutputStream(nulOut, new Adler32());
        Set<String> names = new HashSet<>();
        for (DirectoryMember r : childDirEntries) {
            String name = r.getName();
            if (names.contains(name)) {
                throw new RuntimeException("Name not unique within collection: " + name);
            }
            names.add(name);
            ItemVersion version = r.getMemberItem();
            Item item = version.getItem();
            String line = HashUtils.toHashableText(name, version.getItemHash(), item.getType());
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
            String line = HashUtils.toHashableText(r.getName(), r.getEntryHash(), type);
            HashUtils.appendLine(line, cout);
        }
        cout.flush();
        Checksum check = cout.getChecksum();
        long crc = check.getValue();
        return crc;
    }

}
