package org.spliffy.server.db;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.*;
import org.hibernate.Session;

/**
 * A repository
 *
 * @author brad
 */
@javax.persistence.Entity
@DiscriminatorColumn(name = "TYPE", discriminatorType = DiscriminatorType.STRING, length = 20)
@DiscriminatorValue("R")
@Inheritance(strategy = InheritanceType.JOINED)
public class Repository implements Serializable {

    private long id;
    private String name;
    private List<Branch> branches;
    private BaseEntity baseEntity;
    private Date createdDate;
    private Commit head;

    @Id
    @GeneratedValue
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    /**
     * Each repository to linked to some kind of entity, either a user, a group
     * or an organisation
     *
     * @return
     */
    @ManyToOne(optional = false)
    public BaseEntity getBaseEntity() {
        return baseEntity;
    }

    public void setBaseEntity(BaseEntity user) {
        this.baseEntity = user;
    }

    @Column(length = 255)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "repository")
    public List<Branch> getBranches() {
        return branches;
    }

    public void setBranches(List<Branch> versions) {
        this.branches = versions;
    }

    @Temporal(javax.persistence.TemporalType.DATE)
    @Column(nullable = false)
    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Commit latestVersion(Session session) {
        return trunk(session).getHead();
    }

    public Branch trunk(Session session) {
        if (getBranches() != null) {
            for (Branch b : getBranches()) {
                if (Branch.TRUNK.equals(b.getName())) {
                    return b;
                }
            }
        }
        Branch b = new Branch();
        b.setName(Branch.TRUNK);
        b.setRepository(this);
        session.save(b);
        if (this.branches == null) {
            setBranches(new ArrayList<Branch>());
        }
        getBranches().add(b);
        return b;
    }
}
