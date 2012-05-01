package org.spliffy.server.db;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
public class Item implements Serializable {

    private long id;
    private String type; // "f" = file, "d" = directory
    private Date createDate;
    private List<Permission> grantedPermissions;
    private List<ItemVersion> versions;

    public static Item find(UUID id) {
        return (Item) SessionManager.session().get(Item.class, id);
    }

    public Item() {
    }

    /**
     * Find the permissions on this item which are granted to the given user
     *
     * @param user
     * @return
     */
    public List<Permission> grantedPermissions(User user) {
        List<Permission> list = new ArrayList<>();
        for (Permission p : getGrantedPermissions()) {
            if (p.getGrantee() == user) {
                list.add(p);
            }
        }
        return list;
    }

    @Id
    @GeneratedValue
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Column(nullable = false, length = 1)
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Temporal(javax.persistence.TemporalType.DATE)
    @Column(nullable = false)
    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "grantedOn")
    public List<Permission> getGrantedPermissions() {
        return grantedPermissions;
    }

    public void setGrantedPermissions(List<Permission> grantedPermissions) {
        this.grantedPermissions = grantedPermissions;
    }

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "item")
    public List<ItemVersion> getVersions() {
        return versions;
    }

    public void setVersions(List<ItemVersion> versions) {
        this.versions = versions;
    }



}
