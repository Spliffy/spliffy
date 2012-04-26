package org.spliffy.server.db;

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

    @Column
    public String getPasswordDigest() {
        return passwordDigest;
    }

    public void setPasswordDigest(String password) {
        this.passwordDigest = password;
    }


    
    
}
