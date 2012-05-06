package org.spliffy.server.db;

import java.util.List;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 *
 * @author brad
 */
@javax.persistence.Entity
@Table(name="USER_ENTITY")
@DiscriminatorValue("U")
public class User extends BaseEntity {
    
    private String password;
    
    private String email;

    @Column
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Column(nullable=false)
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }    
}
