package org.spliffy.server.db;

import java.io.Serializable;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 *
 * @author brad
 */
@Entity
public class BlobHash implements Serializable {
    private long blobHash;
    private UUID volumeId;
    
    public static BlobHash findByHash(long hash) {
        return (BlobHash) SessionManager.session().get(BlobHash.class, hash);
    }    
    

    @Id
    public long getBlobHash() {
        return blobHash;
    }

    public void setBlobHash(long hash) {
        this.blobHash = hash;
    }

    @Column(nullable=false)
    public UUID getVolumeId() {
        return volumeId;
    }

    public void setVolumeId(UUID volumeId) {
        this.volumeId = volumeId;
    }
    
    
}
