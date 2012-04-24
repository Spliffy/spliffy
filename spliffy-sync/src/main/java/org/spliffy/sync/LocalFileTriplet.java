package org.spliffy.sync;

import java.io.File;
import org.hashsplit4j.api.FileBlobStore;
import org.spliffy.common.FileTriplet;

/**
 *
 * @author brad
 */
public class LocalFileTriplet extends FileTriplet {
    private final FileBlobStore blobStore;

    public LocalFileTriplet(File file) {
        blobStore = new FileBlobStore(file);
    }

    public FileBlobStore getBlobStore() {
        return blobStore;
    }
    
    public File getFile() {
        return blobStore.getFile(); 
    }
                
}
