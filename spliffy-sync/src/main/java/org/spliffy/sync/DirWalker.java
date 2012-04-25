package org.spliffy.sync;

import com.bradmcevoy.common.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.spliffy.common.FileTriplet;

/**
 * Walks two directory structures, looking for differences, and invoking methods
 * on the given DeltaListener to resolve differences
 *
 * @author brad
 */
public class DirWalker {

    private final TripletStore remoteTripletStore;
    private final TripletStore localTripletStore;
    private final SyncStatusStore syncStatusStore;
    private DeltaListener2 deltaListener;
    
    private final List<LocalDelete> localDeletes = new ArrayList<>();

    public DirWalker(TripletStore remoteTripletStore, TripletStore localTripletStore, SyncStatusStore syncStatusStore, DeltaListener2 deltaListener) {
        this.remoteTripletStore = remoteTripletStore;
        this.localTripletStore = localTripletStore;
        this.syncStatusStore = syncStatusStore;
        this.deltaListener = deltaListener;
    }


    public void walk() {
        walk(Path.root());
        processLocalDeletes(); // we want to leave deletes until last in case there's some bytes we can use
    }

    private void walk(Path path) {
        List<FileTriplet> remoteTriplets = remoteTripletStore.getTriplets(path);
        Map<String, FileTriplet> remoteMap = toMap(remoteTriplets);
        List<FileTriplet> localTriplets = localTripletStore.getTriplets(path);
        Map<String, FileTriplet> localMap = toMap(remoteTriplets);

        if (remoteTriplets != null) {
            for (FileTriplet remoteTriplet : remoteTriplets) {
                Path childPath = path.child(remoteTriplet.getName());
                FileTriplet localTriplet = localMap.get(remoteTriplet.getName());
                if (localTriplet == null) {
                    doMissingLocal(remoteTriplet, childPath);
                } else {
                    if (localTriplet.getHash() == remoteTriplet.getHash()) {
                        // clean, nothing to do
                    } else {
                        doDifferentHashes(remoteTriplet, localTriplet, childPath);
                    }
                }
            }
        }
        
        // Now look for local resources which do not match (by name) remote resources
        if( localTriplets != null ) {
            for( FileTriplet localTriplet : localTriplets) {
                if( !remoteMap.containsKey(localTriplet.getName())) {
                    Path childPath = path.child(localTriplet.getName());
                    doMissingRemote(localTriplet, childPath);
                }
            }
        }

    }

    private Map<String, FileTriplet> toMap(List<FileTriplet> triplets) {
        Map<String, FileTriplet> map = new HashMap<>();
        if (triplets != null) {
            for (FileTriplet r : triplets) {
                map.put(r.getName(), r);
            }
        }
        return map;
    }

    /**
     * Called when there is a remote resource with no matching local resource
     *
     * Possibilities are: - it was in both local and remote, but has been
     * locally deleted - it is remotely new
     *
     * @param remoteTriplet
     * @param path
     */
    private void doMissingLocal(FileTriplet remoteTriplet, Path path) {
        Long localPreviousHash = syncStatusStore.findBackedUpHash(path);
        if (localPreviousHash == null) {
            // not previously synced, so is remotely new
            deltaListener.onRemoteChange(remoteTriplet, remoteTriplet, path);
        } else {
            // was previously synced, now locally gone, so must have been deleted (or moved, same thing)
            deltaListener.onLocalDeletion(path, remoteTriplet);
        }
    }

    /**
     * Called when there are local and remote resources with the same path, but
     * with different hashes
     *
     * Possibilities: - both are directories: so just continue the scan - both
     * are files - remote modified, local unchanged = downSync - remote
     * unchanged, local modified = upSync - both changed = file conflict - one
     * is a file, the other a directory = tree conflict
     *
     * @param remoteTriplet
     * @param localTriplet
     * @param path
     */
    private void doDifferentHashes(FileTriplet remoteTriplet, FileTriplet localTriplet, Path path) {
        if (remoteTriplet.isDirectory() && localTriplet.isDirectory()) {
            walk(path);  // both directories, so continue
        } else if (!remoteTriplet.isDirectory() && !localTriplet.isDirectory()) {
            // both files, check for consistency
            Long localPreviousHash = syncStatusStore.findBackedUpHash(path);
            if (localPreviousHash == null) {
                // not previously synced, so is remotely new. But is different to server, so which is the latest? = conflict
                deltaListener.onFileConflict(remoteTriplet, localTriplet, path);
            } else {
                if (localPreviousHash.longValue() == localTriplet.getHash()) {
                    // local copy is unchanged from last sync, so we can safely down sync
                    deltaListener.onRemoteChange(remoteTriplet, localTriplet, path);
                } else {
                    // local has changed from last sync, but server is different again. Clearly a CONFLICT
                    deltaListener.onFileConflict(remoteTriplet, localTriplet, path);
                }
            }
        } else {
            deltaListener.onTreeConflict(remoteTriplet, localTriplet, path);
        }
    }

    /**
     * Called when there is a local resource with no matching (by name)
     * remote resource
     * 
     * Possibilities:
     *  - the resource has been added locally
     *      - if the resource is a directory we continue scan
     *      - if a file we upSync it
     *  - the resource has been remotely deleted
     * 
     * @param localTriplet
     * @param childPath 
     */
    private void doMissingRemote(FileTriplet localTriplet, Path path) {
        Long localPreviousHash = syncStatusStore.findBackedUpHash(path);
        if( localPreviousHash == null ) {
            // locally new
            deltaListener.onLocalChange(localTriplet, path);  // if resource is a directory this should create it
            if( localTriplet.isDirectory()) {  // continue scan
                walk(path);
            }
        } else {
            // it was previously synced, but now gone. So must have been deleted remotely            
            // So we want to "delete" the local resource. But its possible this is half
            // of a move operation, so instead of immediately deleting we will defer it
            LocalDelete localDelete = new LocalDelete(localTriplet, path);
            localDeletes.add(localDelete);
        }
    }

    private void processLocalDeletes() {
        for( LocalDelete del : localDeletes ) {
            deltaListener.onRemoteDelete(del.localTriplet, del.path);
        }
    }
    
    /**
     * We want to defer local deletes until the end of the scan, because what looks
     * like a delete might actually be a move. By leaving the file in place
     * when we see a local add (downSync) we will have the bytes in place to generate
     * that file
     */
    class LocalDelete {
        final FileTriplet localTriplet;
        final Path path;

        LocalDelete(FileTriplet localTriplet, Path path) {
            this.localTriplet = localTriplet;
            this.path = path;
        }                
    }
}
