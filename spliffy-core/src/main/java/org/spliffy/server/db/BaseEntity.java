package org.spliffy.server.db;

import com.ettrema.http.AccessControlledResource;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.*;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.spliffy.server.db.utils.SessionManager;

/**
 * Represents a real world entity such as a user or an organisation
 *
 * Any type of Entity can contain Repository objects.
 *
 * An entity can be both a recipient of permissions and a target of permissions.
 * For example a user entity can be given access to an organisation entity
 * 
 * An entity name must be unique within the organisation that defines it. What this
 * name means depends on the context. For a user, the name is almost meaningless
 *
 * @author brad
 */
@javax.persistence.Entity
@Table(name = "BASE_ENTITY",
uniqueConstraints = {
    @UniqueConstraint(columnNames = {"name", "organisation"})}// item names must be unique within a directory
)
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "TYPE", discriminatorType = DiscriminatorType.STRING, length = 20)
@DiscriminatorValue("E")
public class BaseEntity implements Serializable {

    public static BaseEntity find(Organisation org, String name, Session session) {
        Criteria crit = session.createCriteria(BaseEntity.class);
        crit.add(Expression.and(Expression.eq("organisation", org), Expression.eq("name", name)));        
        List list = crit.list();
        if( list == null || list.isEmpty() ) {
            System.out.println("nothignn found");
            return null;
        } else {
            return (BaseEntity) list.get(0);
        }
    }
       
    private long id;
    private String name;
    private String type;
    private Organisation organisation;
    private Date createdDate;
    private Date modifiedDate;
    private List<Permission> grantedPermissions; // can be granted permissions
    private List<Repository> repositories;    // has repositories
    private List<GroupMembership> memberships; // can belong to groups
    private List<NvPair> nvPairs; // holds data capture information
    private List<AddressBook> addressBooks; // has addressbooks
    private List<Calendar> calendars; // has calendars

    @Id
    @GeneratedValue
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @ManyToOne
    public Organisation getOrganisation() {
        return organisation;
    }

    public void setOrganisation(Organisation organisation) {
        this.organisation = organisation;
    }
    
    

    @Column(nullable = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "grantee")
    public List<Permission> getGrantedPermissions() {
        return grantedPermissions;
    }

    public void setGrantedPermissions(List<Permission> grantedPermissions) {
        this.grantedPermissions = grantedPermissions;
    }

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "baseEntity")
    public List<Repository> getRepositories() {
        return repositories;
    }

    public void setRepositories(List<Repository> repositories) {
        this.repositories = repositories;
    }

    @Column(nullable = false)
    @Temporal(javax.persistence.TemporalType.DATE)
    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    @Column(nullable = false)
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
    public boolean containsUser(Profile user) {
        return user.getName().equals(this.getName()); // very simple because currenly only have users
    }

    @OneToMany(mappedBy = "owner")
    public List<Calendar> getCalendars() {
        return calendars;
    }

    public void setCalendars(List<Calendar> calendars) {
        this.calendars = calendars;
    }

    @OneToMany(mappedBy = "owner")
    public List<AddressBook> getAddressBooks() {
        return addressBooks;
    }

    public void setAddressBooks(List<AddressBook> addressBooks) {
        this.addressBooks = addressBooks;
    }

    @OneToMany(mappedBy = "baseEntity")
    public List<NvPair> getNvPairs() {
        return nvPairs;
    }

    public void setNvPairs(List<NvPair> nvPairs) {
        this.nvPairs = nvPairs;
    }

    @Column
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    
    
    @OneToMany(mappedBy = "member")
    public List<GroupMembership> getMemberships() {
        return memberships;
    }

    public void setMemberships(List<GroupMembership> memberships) {
        this.memberships = memberships;
    }

    /**
     * Give this entity the given priviledge to access the grantedOn entity
     *
     * @param priviledge
     * @param grantedOn
     */
    public void grant(AccessControlledResource.Priviledge priviledge, BaseEntity grantedOn) {
        if (isGranted(priviledge, grantedOn)) {
            return;
        }
        Permission p = new Permission();
        p.setGrantedOnEntity(grantedOn);
        p.setGrantee(this);
        p.setPriviledge(priviledge);
        SessionManager.session().save(p);
    }

    public boolean isGranted(AccessControlledResource.Priviledge priviledge, BaseEntity grantedOn) {
        Session session = SessionManager.session();
        Criteria crit = session.createCriteria(Permission.class);
        crit.add(Expression.and(Expression.eq("grantedOnEntity", grantedOn), Expression.eq("priviledge", priviledge)));
        List list = crit.list();
        return list != null && !list.isEmpty();
    }
    
    
}
