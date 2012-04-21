package org.spliffy.server.db;

import java.util.Date;
import java.util.List;
import javax.persistence.*;

/**
 *
 * @author brad
 */
@javax.persistence.Entity
@Table(name="USER_ENTITY")
@DiscriminatorValue("U")
public class User extends BaseEntity {

    private String passwordDigest;
    private List<Repository> repositories;
    private Date createdDate;
    private Date modifiedDate;

    @Column
    public String getPasswordDigest() {
        return passwordDigest;
    }

    public void setPasswordDigest(String password) {
        this.passwordDigest = password;
    }

    @OneToMany(cascade= CascadeType.ALL, mappedBy="user")
    public List<Repository> getRepositories() {
        return repositories;
    }

    public void setRepositories(List<Repository> repositories) {
        this.repositories = repositories;
    }

    @Temporal(javax.persistence.TemporalType.DATE)
    @Column(nullable=false)
    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    @Temporal(javax.persistence.TemporalType.DATE)
    @Column(nullable=false)
    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }
    
    
    
    
    
}
