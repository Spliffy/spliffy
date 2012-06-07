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
    private String name; // identifies the resource to webdav
    private String title; // user friendly title
    private List<Branch> branches;
    private BaseEntity baseEntity;
    private Date createdDate;
    private Commit head;
    private List<NvPair> nvPairs; // holds data capture information

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

    @Column(length = 255, nullable=false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(length=255)
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    
    
    
    @OneToMany(mappedBy = "repository")
    public List<NvPair> getNvPairs() {
        return nvPairs;
    }

    public void setNvPairs(List<NvPair> nvPairs) {
        this.nvPairs = nvPairs;
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
    
    public Repository setAttribute(String name, String value, Session session) {
        List<NvPair> list = getNvPairs();
        if( list == null ) {
            list = new ArrayList<>();
            setNvPairs(list);
        }
        for( NvPair nv : list ) {
            if( nv.getName().equals(name)) {
                nv.setPropValue(value);
                session.save(nv);
            }
        }
        NvPair nv = new NvPair();
        nv.setRepository(this);
        nv.setName(name);
        nv.setPropValue(value);
        list.add(nv);
        session.save(nv);
        return this;
    }
    
    public String getAttribute(String name) {
        List<NvPair> list = getNvPairs();
        if( list == null ) {
            return null;
        }
        for( NvPair nv : list ) {
            if( nv.getName().equals(name)) {
                return nv.getPropValue();
            }
        }
        return null;
    }
}
