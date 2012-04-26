package org.spliffy.server.db;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.persistence.*;

/**
 * Represents a version of an item in a filesystem. The version is represented
 * by the itemHash.
 * 
 * The parent item object defines whether this is a file or directory, the meaning
 * of the hash varies depending on that. If a file the hash is the hash of the file
 * content. If a directory the hash is that of that formatted DirectoryMember list
 * 
 * 
 * @author brad
 */
@javax.persistence.Entity
public class ItemVersion implements Serializable {

    public static ItemVersion find(UUID metaId) {
        return (ItemVersion) SessionManager.session().get(ItemVersion.class, metaId);
    }
    
      
    private long id;
    private Item item;
    private Date modifiedDate;    
    private long itemHash;
    private List<DirectoryMember> members;

    public ItemVersion() {
    }

    @ManyToOne(optional=false)
    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }
        
    
    
    @Id
    @GeneratedValue
    public long getId() {
        return id;
    }

    public void setId(long id) {
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
    public long getItemHash() {
        return itemHash;
    }

    public void setItemHash(long itemHash) {
        this.itemHash = itemHash;
    }

    @OneToMany(mappedBy="parentItem")
    public List<DirectoryMember> getMembers() {
        return members;
    }

    public void setMembers(List<DirectoryMember> members) {
        this.members = members;
    }
    
    
}
