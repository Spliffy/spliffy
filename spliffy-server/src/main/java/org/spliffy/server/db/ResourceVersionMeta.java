package org.spliffy.server.db;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;
import javax.persistence.*;

/**
 * For holding permissions, etc
 * 
 * Note included in hash of directory entries
 * 
 * 
 *
 * @author brad
 */
@javax.persistence.Entity
public class ResourceVersionMeta implements Serializable {

    public static ResourceVersionMeta find(UUID metaId) {
        return (ResourceVersionMeta) MiltonOpenSessionInViewFilter.session().get(ResourceVersionMeta.class, metaId);
    }
    
      
    private UUID id;
    private ResourceMeta resourceMeta;
    private Date modifiedDate;    
    private long repoVersionNum;

    public ResourceVersionMeta() {
    }

    @ManyToOne
    public ResourceMeta getResourceMeta() {
        return resourceMeta;
    }

    public void setResourceMeta(ResourceMeta resourceMeta) {
        this.resourceMeta = resourceMeta;
    }
        
    
    
    @Id
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

   
    @Temporal(javax.persistence.TemporalType.DATE)
    @Column(nullable=false)
    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    /**
     * Identifies the repository version number in which this change was made
     * 
     * @return 
     */
    @Column(nullable=false)
    public long getRepoVersionNum() {
        return repoVersionNum;
    }

    public void setRepoVersionNum(long repoVersionNum) {
        this.repoVersionNum = repoVersionNum;
    }
    
    
}
