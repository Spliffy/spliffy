package org.spliffy.common;

import java.io.*;
import java.util.*;
import java.util.zip.CheckedOutputStream;
import org.hashsplit4j.api.NullBlobStore;
import org.hashsplit4j.api.NullHashStore;
import org.hashsplit4j.api.Parser;

/**
 *
 * @author brad
 */
public class HashUtils {
    /**
     * 
     * @param name - the name of the resource as it appears withint the current directory
     * @param crc - the hash of the resource. Either the crc of the file, of the hashed value of its members if a directory (ie calculated with this method)
     * @param type - "f" = file, "d" = directory
     * @return 
     */
    public static String toHashableText(String name, Long crc, String type) {
        String line = name + ":" + crc + ":" + type  + '\n';
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
    
    public static List<FileTriplet> parseTriplets(InputStream in) throws IOException {
        Reader reader = new InputStreamReader(in);
        BufferedReader bufIn = new BufferedReader(reader);
        List<FileTriplet> list = new ArrayList<>();
        String line = bufIn.readLine();
        while( line != null ) {
            FileTriplet triplet = parse(line);
            list.add(triplet);
            line = bufIn.readLine();
        }
        return list;
    }

    private static FileTriplet parse(String line) {
        String[] arr = line.split(":");
        FileTriplet triplet = new FileTriplet();
        triplet.setName(arr[0]);
        triplet.setHash(Long.parseLong(arr[1]));
        triplet.setType(arr[3]);
        return triplet;
    }
    
    public static Map<String, FileTriplet> toMap(List<FileTriplet> triplets) {
        Map<String,FileTriplet> map = new HashMap<>();
        for( FileTriplet t : triplets) {
            map.put(t.getName(), t);
        }
        return map;
    }

    public static void verifyHash(File f, long expectedHash) throws IOException {
        try (FileInputStream fin = new FileInputStream(f); BufferedInputStream bufIn = new BufferedInputStream(fin)) {            
            Parser parser = new Parser();
            NullBlobStore blobStore = new NullBlobStore();
            NullHashStore hashStore = new NullHashStore();
            long actualHash = parser.parse(bufIn, hashStore, blobStore);
            if( actualHash != expectedHash) {
                throw new IOException("File does not have the expected hash value: Expected: " + expectedHash + " actual:" + actualHash );
            }
        }
    }
}
