package org.spliffy.server.db;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;

/**
 * A RepoVersion is a link between a Repository and an ItemVersion
 * 
 * The ItemVersion linked to is a directory, and its members are the 
 * members of the Repository for this version
 * 
 * The latest version for a Repository (ie with the highest versionNum)
 * is the current version of the repository (ie the Head)
 *
 * @author brad
 */
@javax.persistence.Entity
public class RepoVersion implements Serializable {
    private long id;
    private ItemVersion rootItemVersion; // this is the root directory for the repository (in this version)
    private long versionNum;
    // parent
    private Repository repo;
    private Date createdDate; 
           
    /**
     * The user which created this version
     */
    private User editor;

    public RepoVersion() {
    }
        
    @ManyToOne(optional=false)
    public ItemVersion getRootItemVersion() {
        return rootItemVersion;
    }

    public void setRootItemVersion(ItemVersion rootItemVersion) {
        this.rootItemVersion = rootItemVersion;
    }    
    
    @ManyToOne(optional=false)    
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
    @GeneratedValue
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    /**
     * Monotonically increasing version number for the repository. Might
     * not be sequential
     * 
     * @return 
     */
    @Column(nullable=false)
    public long getVersionNum() {
        return versionNum;
    }

    public void setVersionNum(long versionNum) {
        this.versionNum = versionNum;
    }
    
    
}
