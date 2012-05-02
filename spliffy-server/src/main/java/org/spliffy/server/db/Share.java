package org.spliffy.server.db;

import com.ettrema.http.AccessControlledResource;
import com.ettrema.http.AccessControlledResource.Priviledge;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;
import javax.persistence.*;
import org.hibernate.Session;

/**
 * Represents a symbolic link, or shared folder
 *
 * A Link is a pointer to the Item of the shared folder. When the Share
 * invitation is accepted it is connected to a new resource in the recipients
 * repository, and the sharedTo value is set
 *
 * The folder may be un-shared by deleting the Share record, but the recipient
 * still has the folder. But once it is unshared, the original users changes are
 * not visible, and the recipient cannot modify it
 *
 * Note that the existence of a share does not by itself convey any priviledges,
 * it merely places the folder in the recipients workspace. A Permission must
 * also be created
 *
 * TODO: extend this to support cross-server links. This will require specifying
 * the address of the remote system.
 *
 * @author brad
 */
@Entity
public class Share implements Serializable {

    public static Share get(UUID id, Session session) {
        return (Share) session.get(Share.class, id);
    }

    private UUID id;
    private Item sharedFrom;
    private User acceptedBy;
    private AccessControlledResource.Priviledge priv;
    private String shareRecip;
    private Date createdDate;
    private Date acceptedDate;

    /**
     * Use a random UUID so its cryptographically secure. This allows the identifier
     * to be used as a credential
     *
     * @return
     */
    @Id
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @ManyToOne(optional = false)
    public Item getSharedFrom() {
        return sharedFrom;
    }

    public void setSharedFrom(Item sharedFrom) {
        this.sharedFrom = sharedFrom;
    }

    @ManyToOne
    public User getAcceptedBy() {
        return acceptedBy;
    }

    public void setAcceptedBy(User acceptedBy) {
        this.acceptedBy = acceptedBy;
    }
        

    /**
     * For situations where the sharedTo item is set some time after
     * the folder is shared (such as when sending a share over email)
     * this field holds the priviledge to be granted when the share is
     * accepted. It is then used to create the Permission object which will
     * allow access.
     * 
     * Note that this value has only historical meaning after the share has been accepted
     * 
     * @return 
     */
    public Priviledge getPriviledge() {
        return priv;
    }

    public void setPriviledge(Priviledge granted) {
        this.priv = granted;
    }

    /**
     * Optional, to record who this has been shared with. Such as an email
     * address
     * 
     * @return 
     */
    @Column
    public String getShareRecip() {
        return shareRecip;
    }

    public void setShareRecip(String shareRecip) {
        this.shareRecip = shareRecip;
    }

    @Temporal(javax.persistence.TemporalType.DATE)
    public Date getAcceptedDate() {
        return acceptedDate;
    }

    public void setAcceptedDate(Date acceptedDate) {
        this.acceptedDate = acceptedDate;
    }

    @Column(nullable=false)
    @Temporal(javax.persistence.TemporalType.DATE)
    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }
    
    
}
