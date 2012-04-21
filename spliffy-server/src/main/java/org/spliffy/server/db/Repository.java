package org.spliffy.server.db;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.persistence.*;

/**
 *
 * @author brad
 */
@javax.persistence.Entity
public class Repository implements Serializable {
    private UUID id;
    private String name;
    private List<RepoVersion> versions;
    private User user;
    private Date createdDate;

    @Id
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @ManyToOne(optional=false)
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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
        System.out.println("LatestVersion");
        List<RepoVersion> vs = getVersions();
        if( vs == null ) {
            return null;
        }
        RepoVersion cur = null;
        for( RepoVersion v : vs ) {
            System.out.println("  version: " + v.getDirHash() + " - " + v.getCreatedDate().getTime());
            if( cur == null || v.getCreatedDate().after(cur.getCreatedDate())) {
                cur = v;
            }
        }
        if( cur != null ) {
            System.out.println("latest is:  " + cur.getDirHash());
        }
        return cur;
    }
    
    
}
