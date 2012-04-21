package org.spliffy.server.db;

import java.io.Serializable;
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
