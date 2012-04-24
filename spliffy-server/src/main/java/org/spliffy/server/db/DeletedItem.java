package org.spliffy.server.db;

import java.io.Serializable;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * Denormalised links for deleted items to permit easy location and restoration
 *
 * @author brad
 */
@Entity
public class DeletedItem implements Serializable {
    private UUID id;
    private ResourceVersionMeta deletedResource;
    private ResourceVersionMeta deletedFrom;
    private RepoVersion repoVersion;

    @Id
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
       
    @ManyToOne
    public ResourceVersionMeta getDeletedFrom() {
        return deletedFrom;
    }

    public void setDeletedFrom(ResourceVersionMeta deletedFrom) {
        this.deletedFrom = deletedFrom;
    }

    @ManyToOne
    public ResourceVersionMeta getDeletedResource() {
        return deletedResource;
    }

    public void setDeletedResource(ResourceVersionMeta deletedResource) {
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
