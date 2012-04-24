package org.spliffy.sync;

import java.io.File;

/**
 * Cache's the last backed up hash for the given file
 *
 * @author brad
 */
public interface LastBackedupStore {

    Long findBackedUpHash(File file);

    public void setBackedupHash(File localFile, long hash);
}
