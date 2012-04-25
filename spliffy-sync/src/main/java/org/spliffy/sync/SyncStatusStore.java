package org.spliffy.sync;

import com.bradmcevoy.common.Path;

/**
 * Represents a means of recording what version of a resource was last
 * synced 
 *
 * @author brad
 */
public interface SyncStatusStore {
    
    Long findBackedUpHash(Path path);

    void setBackedupHash(Path path, long hash);    
}
