package org.spliffy.server.db;

import java.io.Serializable;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 *
 * @author brad
 */
@Entity
public class FanoutEntry implements Serializable{
    private UUID id;
    private FanoutHash fanout;
    private long chunkHash;

    @Column
    public long getChunkHash() {
        return chunkHash;
    }

    public void setChunkHash(long chunkHash) {
        this.chunkHash = chunkHash;
    }

    @ManyToOne
    public FanoutHash getFanout() {
        return fanout;
    }

    public void setFanout(FanoutHash fanout) {
        this.fanout = fanout;
    }

    @Id
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
    
    
}
