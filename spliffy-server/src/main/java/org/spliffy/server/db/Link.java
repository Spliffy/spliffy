package org.spliffy.server.db;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * Represents a symbolic link, or shared folder
 * 
 * A Link is a pointer to the Item of the shared folder. When the
 * Share invitation is accepted it is connected to a new resource in the recipients repository,
 * and the sharedTo value is set
 * 
 * The folder may be un-shared by deleting the Share record, but the recipient still
 * has the folder. But once it is unshared, the original users changes are not visible,
 * and the recipient cannot modify it
 * 
 * Note that the existence of a share does not by itself convey any priviledges, it
 * merely places the folder in the recipients workspace. A Permission must also 
 * be created
 * 
 * TODO: extend this to support cross-server links. This will require specifying the
 * address of the remote system.
 *
 * @author brad
 */
@Entity
public class Link implements Serializable {
    private long id;
    private Item sharedFrom;
    private Item sharedTo;


    @Id
    @GeneratedValue
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @ManyToOne(optional=false)
    public Item getSharedFrom() {
        return sharedFrom;
    }

    public void setSharedFrom(Item sharedFrom) {
        this.sharedFrom = sharedFrom;
    }

    @ManyToOne(optional=true)
    public Item getSharedTo() {
        return sharedTo;
    }

    public void setSharedTo(Item sharedTo) {
        this.sharedTo = sharedTo;
    }
               
}
