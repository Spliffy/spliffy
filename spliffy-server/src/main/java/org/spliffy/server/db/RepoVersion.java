package org.spliffy.server.db;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;

/**
 *
 * @author brad
 */
@javax.persistence.Entity
public class RepoVersion implements Serializable {
    private UUID id;
    private long dirHash;
    // parent
    private Repository repo;
    private Date createdDate; 
           
    /**
     * The user which created this version
     */
    private User editor;

    public RepoVersion() {
    }
        
    public long getDirHash() {
        return dirHash;
    }

    public void setDirHash(long dirHash) {
        this.dirHash = dirHash;
    }    
    
    @ManyToOne
    public Repository getRepository() {
        return repo;
    }

    public void setRepository(Repository repo) {
        this.repo = repo;
    }

    @ManyToOne
    public User getEditor() {
        return editor;
    }

    public void setEditor(User editor) {
        this.editor = editor;
    }

    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    @Id
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}
