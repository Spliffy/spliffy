package org.spliffy.sync;

import com.bradmcevoy.common.Path;
import org.spliffy.common.FileTriplet;

/**
 * A "delta" here refers to some difference between the client and server
 * file systems.
 * 
 *
 * @author brad
 */
public interface DeltaListener2 {

    
    void onLocalDeletion(Path path, FileTriplet remoteTriplet);
    
    void onLocalChange(FileTriplet localTriplet, Path path);

    void onRemoteChange(FileTriplet remoteTriplet, FileTriplet localTriplet, Path path);
    
    void onRemoteDelete(FileTriplet localTriplet, Path path);
    
    void onTreeConflict(FileTriplet remoteTriplet, FileTriplet localTriplet, Path path);

    void onFileConflict(FileTriplet remoteTriplet, FileTriplet localTriplet, Path path);
     
}
