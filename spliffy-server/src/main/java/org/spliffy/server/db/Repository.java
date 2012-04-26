package org.spliffy.server.db;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.*;

/**
 *
 * @author brad
 */
@javax.persistence.Entity
public class Repository implements Serializable {
    private long id;
    private String name;
    private List<RepoVersion> versions;
    private BaseEntity baseEntity;
    private Date createdDate;

    @Id
    @GeneratedValue
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    /**
     * Each repository to linked to some kind of entity, either a user,
     * a group or an organisation
     * 
     * @return 
     */
    @ManyToOne(optional=false)
    public BaseEntity getBaseEntity() {
        return baseEntity;
    }

    public void setBaseEntity(BaseEntity user) {
        this.baseEntity = user;
    }
            
    @Column(length=255)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @OneToMany(cascade= CascadeType.ALL, mappedBy="repository")
    public List<RepoVersion> getVersions() {
        return versions;
    }

    public void setVersions(List<RepoVersion> versions) {
        this.versions = versions;
    }

    @Temporal(javax.persistence.TemporalType.DATE)
    @Column(nullable=false)
    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }
       
    public RepoVersion latestVersion() {
        List<RepoVersion> vs = getVersions();
        if( vs == null ) {
            return null;
        }
        RepoVersion cur = null;
        for( RepoVersion v : vs ) {
            if( cur == null || v.getCreatedDate().after(cur.getCreatedDate())) {
                cur = v;
            }
        }
        if( cur == null ) {
            System.out.println("No RepoVersion for: " + getName());
        }
//        if( cur != null ) {
//            System.out.println("latest is:  " + cur.getDirHash());
//        }
        return cur;
    }
    
    
}
