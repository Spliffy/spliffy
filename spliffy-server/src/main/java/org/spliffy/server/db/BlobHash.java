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
    private String volumeId;
    
    public static BlobHash findByHash(long hash) {
        return (BlobHash) MiltonOpenSessionInViewFilter.session().get(BlobHash.class, hash);
    }    
    

    @Id
    public long getBlobHash() {
        return blobHash;
    }

    public void setBlobHash(long hash) {
        this.blobHash = hash;
    }

    @Column(length = 20)
    public String getVolumeId() {
        return volumeId;
    }

    public void setVolumeId(String volumeId) {
        this.volumeId = volumeId;
    }
    
    
}
