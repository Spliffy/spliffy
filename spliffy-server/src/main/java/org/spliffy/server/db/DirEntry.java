package org.spliffy.server.db;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Id;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

/**
 * A DirEntry is an item with a DirHash
 * 
 * A list of DirEntry objects makes up a tree node
 *
 * @author brad
 */
@javax.persistence.Entity
public class DirEntry implements Serializable {
    private UUID id;
    private String name;
    private long parentHash; // this identifies the parent, by its hash
    private long entryHash; // this is the hash of this item
    private UUID metaId;

    /**
     * Gets the ordered list of entries for the given directory
     * 
     * @param session
     * @param dirHash
     * @return 
     */
    public static List<DirEntry> listEntries(Session session, long dirHash) {
        Criteria crit = session.createCriteria(DirEntry.class);
        crit.add(Restrictions.eq("parentHash", dirHash));
        crit.addOrder(Order.asc("id"));
        List<DirEntry> dirList = new ArrayList<>();
        List oList = crit.list();
        if( oList != null ) {
            dirList.addAll(oList);
        }
        return dirList;
    }
    
    /**
     * @return the name
     */
    @Column(length=1000)
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the hash
     */    
    @Column(nullable=false)
    public long getParentHash() {
        return parentHash;
    }

    /**
     * @param hash the hash to set
     */
    public void setParentHash(long parentDirHash) {
        this.parentHash = parentDirHash;
    }

    /**
     * @return the metaId
     */
    public UUID getMetaId() {
        return metaId;
    }

    /**
     * @param metaId the metaId to set
     */
    public void setMetaId(UUID metaId) {
        this.metaId = metaId;
    }

    /**
     * Artificial PK to keep hibernate happy
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

    @Column(nullable=false)    
    public long getEntryHash() {
        return entryHash;
    }

    public void setEntryHash(long entryHash) {
        this.entryHash = entryHash;
    }

    
}
