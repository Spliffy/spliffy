package org.spliffy.server.db;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
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
public class ResourceMeta implements Serializable {
    private UUID id;
    private String type; // "f" = file, "d" = directory
    private Date createDate;
    private Date modifiedDate;
    private List<Permission> grantedPermissions;

    public static ResourceMeta find(UUID id) {
        return (ResourceMeta) MiltonOpenSessionInViewFilter.session().get(ResourceMeta.class, id);
    }

    public ResourceMeta() {
    }
        
    
    @Id
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @Column(nullable=false, length=1)
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
    

    @Temporal(javax.persistence.TemporalType.DATE)
    @Column(nullable=false)
    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    @Temporal(javax.persistence.TemporalType.DATE)
    @Column(nullable=false)
    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    @OneToMany(cascade= CascadeType.ALL, mappedBy="resourceMeta")
    public List<Permission> getGrantedPermissions() {
        return grantedPermissions;
    }

    public void setGrantedPermissions(List<Permission> grantedPermissions) {
        this.grantedPermissions = grantedPermissions;
    }
    
    
        
    
}
