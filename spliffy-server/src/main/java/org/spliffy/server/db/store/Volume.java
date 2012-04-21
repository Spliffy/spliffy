package org.spliffy.server.db.store;

/**
 * An interface for storing and retrieving bytes
 *
 * @author brad
 */
public interface Volume {

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
