package org.spliffy.server.db;

import java.io.Serializable;
import java.util.List;
import javax.persistence.*;

/**
 * Represents a real world entity such as a user or an organisation
 *
 * @author brad
 */

@javax.persistence.Entity
@Table(name="BASE_ENTITY")
@Inheritance(strategy=InheritanceType.JOINED)
@DiscriminatorColumn(name="TYPE", discriminatorType=DiscriminatorType.STRING,length=20)
@DiscriminatorValue("E")
public class BaseEntity implements Serializable {
    private String name;
    private List<Permission> grantedPermissions;

    @Id
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @OneToMany(cascade= CascadeType.ALL, mappedBy="baseEntity")
    public List<Permission> getGrantedPermissions() {
        return grantedPermissions;
    }

    public void setGrantedPermissions(List<Permission> grantedPermissions) {
        this.grantedPermissions = grantedPermissions;
    }

    
    
}
