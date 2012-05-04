package org.spliffy.server.db;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.*;
import org.hibernate.Session;

/**
 * Represents a real world entity such as a user or an organisation
 * 
 * Any type of Entity can contain Repository objects
 *
 * @author brad
 */

@javax.persistence.Entity
@Table(name="BASE_ENTITY")
@Inheritance(strategy=InheritanceType.JOINED)
@DiscriminatorColumn(name="TYPE", discriminatorType=DiscriminatorType.STRING,length=20)
@DiscriminatorValue("E")
public class BaseEntity implements Serializable {
    private List<Calendar> calendars;

    public static BaseEntity get(String entityName, Session session) {
        return (BaseEntity) session.get(BaseEntity.class, entityName);
    }
    private String name;
    private Date createdDate;
    private Date modifiedDate;
    private List<Permission> grantedPermissions;
    private List<Repository> repositories;    

    @Id
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @OneToMany(cascade= CascadeType.ALL, mappedBy="grantee")
    public List<Permission> getGrantedPermissions() {
        return grantedPermissions;
    }

    public void setGrantedPermissions(List<Permission> grantedPermissions) {
        this.grantedPermissions = grantedPermissions;
    }

    @OneToMany(cascade= CascadeType.ALL, mappedBy="baseEntity")
    public List<Repository> getRepositories() {
        return repositories;
    }

    public void setRepositories(List<Repository> repositories) {
        this.repositories = repositories;
    }

    @Column(nullable=false)
    @Temporal(javax.persistence.TemporalType.DATE)
    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    @Column(nullable=false)
    @Temporal(javax.persistence.TemporalType.DATE)
    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    /**
     * Return true if this entity contains or is the given user
     * 
     * @param user
     * @return 
     */
    public boolean containsUser(User user) {
        return user.getName().equals(this.getName()); // very simple because currenly only have users
    }

    @OneToMany(mappedBy = "owner")
    public List<Calendar> getCalendars() {
        return calendars;
    }

    public void setCalendars(List<Calendar> calendars) {
        this.calendars = calendars;
    }
    
    
}
