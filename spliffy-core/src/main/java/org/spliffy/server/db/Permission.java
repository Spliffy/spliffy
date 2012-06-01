package org.spliffy.server.db;

import com.ettrema.http.AccessControlledResource;
import com.ettrema.http.AccessControlledResource.Priviledge;
import java.io.Serializable;
import javax.persistence.*;

/**
 * Note that in this permission scheme priviledges can only be granted,
 * not revoked.
 * 
 * This makes for a very simple permissions scheme that users can easily understand
 * and avoids the complexity of other file systems where users frequently wonder
 * why they can't access something, due to the complex interaction between granted
 * and revoked permissions in a hierarchy
 * 
 * So, once a permission has been a applied to a certain folder, that permission
 * will apply on all resources under that folder
 * 
 * A Permission can also be applied to an "entity" ie a user or organisation
 *
 * @author brad
 */
@Entity
public class Permission implements Serializable {
        
    private long id;
    private AccessControlledResource.Priviledge priviledge;
    private BaseEntity grantee;
    private Item grantedOn;
    private BaseEntity grantedOnEntity;
        

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

    @OneToOne
    public BaseEntity getGrantedOnEntity() {
        return grantedOnEntity;
    }

    public void setGrantedOnEntity(BaseEntity grantedOnEntity) {
        this.grantedOnEntity = grantedOnEntity;
    }
        
    
}
