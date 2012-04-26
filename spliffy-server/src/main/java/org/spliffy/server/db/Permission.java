package org.spliffy.server.db;

import com.ettrema.http.AccessControlledResource;
import com.ettrema.http.AccessControlledResource.Priviledge;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 *
 * @author brad
 */
@Entity
public class Permission implements Serializable {
    private long id;
    private AccessControlledResource.Priviledge priviledge;
    private BaseEntity grantee;
    private Item grantedOn;
        

    @Id
    @GeneratedValue
    public long getId() {
        return id;
    }

    public void setId(long id) {
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
    public Item getGrantedOn() {
        return grantedOn;
    }

    public void setGrantedOn(Item grantedOn) {
        this.grantedOn = grantedOn;
    }
    
    
    
}
