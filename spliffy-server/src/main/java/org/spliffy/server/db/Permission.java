package org.spliffy.server.db;

import com.ettrema.http.AccessControlledResource;
import com.ettrema.http.AccessControlledResource.Priviledge;
import java.io.Serializable;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 *
 * @author brad
 */
@Entity
public class Permission implements Serializable {
    private UUID id;
    private AccessControlledResource.Priviledge priviledge;
    private BaseEntity grantee;
    private ResourceMeta grantedOn;

    @Id
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Priviledge getPriviledge() {
        return priviledge;
    }

    public void setPriviledge(Priviledge priviledge) {
        this.priviledge = priviledge;
    }

    @ManyToOne
    public BaseEntity getGrantee() {
        return grantee;
    }

    public void setGrantee(BaseEntity grantee) {
        this.grantee = grantee;
    }

    @ManyToOne
    public ResourceMeta getGrantedOn() {
        return grantedOn;
    }

    public void setGrantedOn(ResourceMeta grantedOn) {
        this.grantedOn = grantedOn;
    }
    
    
    
}
