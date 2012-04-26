package org.spliffy.server.db;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * Denormalised links for deleted items to permit easy location and restoration
 *
 * @author brad
 */
@Entity
public class DeletedItem implements Serializable {
    private long id;
    private ItemVersion deletedResource;
    private ItemVersion deletedFrom;
    private RepoVersion repoVersion;

    @Id
    @GeneratedValue
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
       
    @ManyToOne
    public ItemVersion getDeletedFrom() {
        return deletedFrom;
    }

    public void setDeletedFrom(ItemVersion deletedFrom) {
        this.deletedFrom = deletedFrom;
    }

    @ManyToOne
    public ItemVersion getDeletedResource() {
        return deletedResource;
    }

    public void setDeletedResource(ItemVersion deletedResource) {
        this.deletedResource = deletedResource;
    }

    @ManyToOne
    public RepoVersion getRepoVersion() {
        return repoVersion;
    }

    public void setRepoVersion(RepoVersion repoVersion) {
        this.repoVersion = repoVersion;
    }
        
}
