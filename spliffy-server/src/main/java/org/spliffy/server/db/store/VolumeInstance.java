package org.spliffy.server.db.store;

/**
 * An interface for storing and retrieving bytes
 * 
 * A single VolumeInstance refers to a physical location at which bytes
 * can be stored, such as a directory in a file system or a remote HTTP server
 * 
 * These are grouped together to form a Volume. A volume will replicate data
 * among the VolumeInstances in the Volume
 *
 * @author brad
 */
public interface VolumeInstance {

    public void setBlob(long hash, byte[] bytes);

    /**
     * A permanent identifier for this volume. The identifier will be stored along
     * with the blob hash so that the bytes can be retrieved later
     * 
     * @return 
     * 
     */
    public String getId();

    public byte[] getBlob(long hash);
    
}
