package org.spliffy.sync;

import com.bradmcevoy.http.Utils;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.exceptions.NotFoundException;
import com.ettrema.httpclient.HttpException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JOptionPane;
import org.apache.commons.httpclient.HttpClient;
import org.spliffy.common.FileTriplet;
import org.spliffy.common.HashUtils;

/**
 *
 * @author brad
 */
public class DefaultDeltaWalker implements DeltaWalker {

    private final HttpClient httpClient;
    private final LastBackedupStore lastBackedupStore;

    public DefaultDeltaWalker(HttpClient httpClient, LastBackedupStore hashCache) {
        this.httpClient = httpClient;
        this.lastBackedupStore = hashCache;
    }

    @Override
    public Long scanDir(Long remoteDirHash, File localParent, String encodedPath, ScanningHashStore scanningHashStore, DeltaListener listener) throws IOException, HttpException, NotAuthorizedException, BadRequestException, ConflictException {        
        List<FileTriplet> remoteTriplets = getRemoteTriplets(encodedPath);
        for (FileTriplet remoteTriplet : remoteTriplets) {
            File localChild = new File(localParent, remoteTriplet.getName());
            LocalFileTriplet localTriplet = scanningHashStore.getLocalTriplet(localChild);
            if (localTriplet != null && localTriplet.getHash() == remoteTriplet.getHash()) {
                System.out.println("DefaultDeltaWalker: In sync: " + localChild.getAbsolutePath());
            } else {
                System.out.println("DefaultDeltaWalker: Not in sync, or remotely new: " + localChild.getAbsolutePath());
                String childEncodedPath = encodedPath + Utils.percentEncode(remoteTriplet.getName());
                if (remoteTriplet.isDirectory()) {
                    if (!localChild.exists()) {
                        if (!localChild.mkdir()) {
                            throw new RuntimeException("Couldnt create directory: " + localChild.getAbsolutePath());
                        }
                    } else {
                        if (!localChild.isDirectory()) {
                            listener.onTreeConflict(localChild);
                            throw new RuntimeException("cant handle conflicts yet");
                        }
                    }
                    childEncodedPath += "/";
                    scanDir(remoteTriplet.getHash(), localChild, childEncodedPath, scanningHashStore, listener);
                } else {
                    // remote resource is a file and is new or updated. Local file might also be updated which would be a conflict
                    if (localChild.exists()) {
                        if (localChild.isDirectory()) {
                            System.out.println("tree conflict on remote path: " + encodedPath);
                            listener.onTreeConflict(localChild);
                        } else {
                            checkFileUpdate(localChild, childEncodedPath, remoteTriplet, localTriplet, listener);
                        }
                    } else {
                        // remote file exists and local does not. 
                        // If last backed up crc does not exist then is remotely new
                        // If the last backed up crc is same as on the server then it was deleted locally, so we should delete on server.                        
                        // If the last backed crc exists but is different to server then it has been locally deleted and remotely modified = conflict
                        Long lastBackedup = lastBackedupStore.findBackedUpHash(localChild);
                        if (lastBackedup == null) {
                            listener.onRemoteChange(remoteTriplet.getHash(), remoteTriplet.isDirectory(), localChild);
                        } else {
                            if (lastBackedup == remoteTriplet.getHash()) {
                                // locally deleted, clean server copy
                                listener.onLocalDeletetion(localChild, childEncodedPath);
                            } else {
                                // technically a conflict, but simplest thing is to restore latest version
                                listener.onRemoteChange(remoteTriplet.getHash(),remoteTriplet.isDirectory(), localChild);
                            }
                        }

                    }
                }
            }
        }
        System.out.println("check locally new stuff");
        // Now check for locally new resources
        Set<String> remoteNames = toSet(remoteTriplets);
        System.out.println("got remote set");
        File[] childFiles = localParent.listFiles();
        if (childFiles != null) {
            System.out.println("child files: " + childFiles.length);
            for (File childFile : childFiles) {
                System.out.println("check local: " + childFile.getAbsolutePath());
                if (!scanningHashStore.ignored(childFile)) {
                    if (!remoteNames.contains(childFile.getName())) {
                        String childEncodedPath = encodedPath;
                        System.out.println("Found local resource not on server: " + childEncodedPath);
                        if (!childEncodedPath.endsWith("/")) {
                            childEncodedPath += "/";
                        }
                        childEncodedPath += Utils.percentEncode(childFile.getName());
                        if (childFile.isDirectory()) {
                            System.out.println("  is dir, continue");
                            scanDir(null, childFile, childEncodedPath, scanningHashStore, listener);
                        } else {
                            System.out.println("  not dir, check for confilct");
                            // there is a local file which is not on the server. This could be
                            // a remote deletion to be applied locally, or it could be a new file to
                            // be uploaded.
                            Long backedUpHash = lastBackedupStore.findBackedUpHash(childFile);
                            if (backedUpHash == null) {
                                // not previously backed up, so is locally new                        
                                listener.onLocalChange(childEncodedPath, childFile);
                            } else {
                                // was previously backed up, but has been deleted on the server, so delete locally
                                listener.onRemoteDeletion(childEncodedPath, childFile);
                            }
                        }
                    }
                }
            }
        }
        throw new RuntimeException("nothing to return yet");
    }

    private List<FileTriplet> getRemoteTriplets(String path) throws IOException, HttpException, NotAuthorizedException, BadRequestException, ConflictException {
        try {
            byte[] arrRemoteTriplets = HttpUtils.get(httpClient, path + "?type=hashes");
            List<FileTriplet> remoteTriplets = HashUtils.parseTriplets(new ByteArrayInputStream(arrRemoteTriplets));
//            for (FileTriplet t : remoteTriplets) {
//                System.out.println(" - " + t.getName());
//            }
            return remoteTriplets;
        } catch (NotFoundException ex) {
            return Collections.EMPTY_LIST;
        }
    }

    /**
     *
     *
     * @param encodedPath
     * @param remoteTriplet
     * @param localTriplet
     * @throws HttpException
     * @throws NotAuthorizedException
     * @throws BadRequestException
     * @throws ConflictException
     * @throws IOException
     */
    private void checkFileUpdate(File localFile, String encodedPath, FileTriplet remoteTriplet, LocalFileTriplet localTriplet, DeltaListener listener) throws HttpException, NotAuthorizedException, BadRequestException, ConflictException, IOException {
        // we need to know: current local hash, current remote hash, hash of the file (ie for metaId) on this revision
        Long backedUpHash = lastBackedupStore.findBackedUpHash(localFile);
        if (backedUpHash == null) {
            // Never backed up, but there is a server file with same name --> CONFLICT
            JOptionPane.showMessageDialog(null, "Conflict: " + localFile.getAbsolutePath() + " because no backed up hash could be found for the local file, but it exists on the server");
            return;
        } else {
            if (localTriplet != null && backedUpHash.longValue() == localTriplet.getHash()) {
                // no local changes, download from server if necessary
                if (remoteTriplet.getHash() == localTriplet.getHash()) {
                    System.out.println("Local file matches server, no change: " + localFile.getAbsolutePath());
                } else {
                    System.out.println("Remote file is updated and local file is unchanged, so download");
                    listener.onRemoteChange(remoteTriplet.getHash(),remoteTriplet.isDirectory(), localFile);
                }
            } else {
                // local file has changed, check for conflict
                if (backedUpHash == remoteTriplet.getHash()) {
                    System.out.println("local file has changed; remote file is same as was last backed up; so upload");
                    listener.onLocalChange(encodedPath, localFile);
                } else {
                    // local file has changed, but remote file has also changed, and they're not the same
                    // -->> CONFLICT!!
                    System.out.println("Conflict: " + localFile.getAbsolutePath());
                    if (localTriplet != null) {
                        System.out.println("current local hash: " + localTriplet.getHash());
                    } else {
                        System.out.println("current local hash: none");
                    }
                    System.out.println("backedup hash: " + backedUpHash);
                    System.out.println("remote hash: " + remoteTriplet.getHash());
                    listener.onFileConflict(remoteTriplet.getHash(), localFile, encodedPath);
                }
            }
        }
    }

    private Set<String> toSet(List<FileTriplet> remoteTriplets) {
        Set<String> set = new HashSet<>();
        for (FileTriplet r : remoteTriplets) {
            set.add(r.getName());
        }
        return set;
    }
}
