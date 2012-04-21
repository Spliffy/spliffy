package org.spliffy.server.db.store;

import java.io.File;

/**
 *
 * @author brad
 */
public class FsHashUtils {
    public static String toHex(long hash) {
        return Long.toHexString(hash);
    }

    public static File toFile(File root, long hash) {
        File f = root;
        for (String hex = toHex(hash); hex.length() > 2; hex = hex.substring(0, 2)) { // hex = b5 07 46 e4
            f = new File(f, hex);
        }
        return f;
    }
}
