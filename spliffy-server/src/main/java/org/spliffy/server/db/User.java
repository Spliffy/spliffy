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
    private List<Share> shares;

    private String passwordDigest;
    
    private String email;

    @Column
    public String getPasswordDigest() {
        return passwordDigest;
    }

    public void setPasswordDigest(String password) {
        this.passwordDigest = password;
    }

    @Column(nullable=false)
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @OneToMany(mappedBy = "acceptedBy")
    public List<Share> getShares() {
        return shares;
    }

    public void setShares(List<Share> shares) {
        this.shares = shares;
    }

    
    
    
}
