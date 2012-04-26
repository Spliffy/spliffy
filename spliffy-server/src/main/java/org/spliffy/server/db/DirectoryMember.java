package org.spliffy.server.db;

import java.io.Serializable;
import javax.persistence.*;

/**
 * A DirectoryMember represents the existence of an item within a particular
 * directory
 *
 * The list of DirectoryMember objects within a directory defines that directory
 *
 * @author brad
 */
@javax.persistence.Entity
@Table(uniqueConstraints = {
    @UniqueConstraint(columnNames = {"name", "parent_item"})}// item names must be unique within a directory
)
public class DirectoryMember implements Serializable {

    private long id;
    private String name;
    private ItemVersion parentItem;
    private ItemVersion memberItem; // this is the hash of this item

    /**
     * @return the name
     */
    @Column(length = 1000)
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
    @ManyToOne(optional = false)
    public ItemVersion getParentItem() {
        return parentItem;
    }

    /**
     * @param hash the hash to set
     */
    public void setParentItem(ItemVersion parentItem) {
        this.parentItem = parentItem;
    }

    @Id
    @GeneratedValue
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @ManyToOne(optional = false)
    public ItemVersion getMemberItem() {
        return memberItem;
    }

    public void setMemberItem(ItemVersion memberItem) {
        this.memberItem = memberItem;
    }
}
