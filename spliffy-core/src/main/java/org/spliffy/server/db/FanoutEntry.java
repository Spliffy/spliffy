package org.spliffy.server.db;

import java.io.Serializable;
import javax.persistence.*;

/**
 *
 * @author brad
 */
@Entity
public class FanoutEntry implements Serializable{
    private long id;
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
    @GeneratedValue
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
    
    
}
