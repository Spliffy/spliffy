package org.spliffy.sync;

import java.io.File;
import java.io.IOException;

/**
 * A "delta" here refers to some difference between the client and server
 * file systems.
 * 
 *
 * @author brad
 */
public interface DeltaListener {
 
    /**
     * Called when there is a new or updated file on the server
     * 
     * @param hash - checksum of remote file
     * @param localChild  - corresponding local file which might, or might not, exist
     */
    void onRemoteChange(long hash, File localChild) throws IOException;

    /**
     * Called when a locally changed file is detected (new or modified)
     * 
     * @param encodedPath - the path to the local resource on the server
     * @param localFile - reference to the local file, which might exist or might not (indicating a local deletion)
     */
    void onLocalChange(String encodedPath, File localFile) throws IOException;

    /**
     * Called when there is a conflict due to the structure of the folders,
     * rather then the content of a file.
     * 
     * This might be due to a remote file corresponding to a local folder, for example
     * 
     * @param localChild 
     */
    void onTreeConflict(File localChild);

    /**
     * Called when a file exists on client and server, but both have been
     * changed since the last sync. This means that to upload or download
     * would overwrite someone's changes.
     * 
     * We need the user to reconcile the situation, effectively choosing which one to take
     * 
     * @param remoteHash - checksum of current remote file
     * @param localFile - the local file which is conflicted
     * @param encodedPath  - path to the remote file
     */
    void onFileConflict(long remoteHash, File localFile, String encodedPath);

    void onLocallyDeleted(File localChild, String childEncodedPath);
}
