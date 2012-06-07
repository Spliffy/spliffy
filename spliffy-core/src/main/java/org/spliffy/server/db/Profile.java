package org.spliffy.server.db;

import java.util.Date;
import java.util.List;
import javax.persistence.*;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.spliffy.server.db.utils.SessionManager;

/**
 * A user is defined within an organisation. Might change this in the future so
 * that the user profile is within an organsiation, but the credentials probably should exist
 * in a global space.
 *
 * @author brad
 */
@javax.persistence.Entity
@DiscriminatorValue("U")
public class Profile extends BaseEntity {
    
    public static Profile find(Organisation org, String name, Session session) {
        Criteria crit = session.createCriteria(Profile.class);
        crit.add(Expression.and(Expression.eq("organisation", org), Expression.eq("name", name)));        
        List list = crit.list();
        if( list == null || list.isEmpty() ) {
            System.out.println("nothignn found");
            return null;
        } else {
            return (Profile) list.get(0);
        }
        //return (Profile) crit.uniqueResult();
    }
            
    
    private List<Credential> credentials;
                
    private String firstName;
    
    private String surName;
    
    private String phone;

    private String email;

    @OneToMany(mappedBy="profile")
    public List<Credential> getCredentials() {
        return credentials;
    }

    public void setCredentials(List<Credential> credentials) {
        this.credentials = credentials;
    }
               
    public void setEmail(String email) {
        this.email = email;
    }

    @Column
    public String getEmail() {
        return email;
    }
    
        
    public String getSurName() {
        return surName;
    }

    public void setSurName(String surName) {
        this.surName = surName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    /**
     * Create a GroupMembership linking this profile to the given group. Is immediately saved
     * 
     * @param g
     * @return 
     */
    public Profile addToGroup(Group g) {
        if( g.isMember(this)) {
            return this;
        }
        GroupMembership gm = new GroupMembership();
        gm.setCreatedDate(new Date());
        gm.setGroupEntity(g);
        gm.setMember(this);
        gm.setModifiedDate(new Date());
        SessionManager.session().save(gm);
        return this;
    }


}
