package org.spliffy.server.web.versions;

import java.util.Date;
import java.util.List;
import org.spliffy.server.db.*;
import org.spliffy.server.web.AbstractResource;
import org.spliffy.server.web.SecurityUtils;

/**
 * Based class for files and directories accessed within a versions folder
 * 
 * These are similar to their Mutable counterparts, but aren't mutable
 *
 * @author brad
 */
public abstract class AbstractVersionResource extends AbstractResource{

    protected final VersionCollectionResource parent;
    
    protected final DirectoryMember directoryMember;

    public AbstractVersionResource(VersionCollectionResource parent, DirectoryMember directoryMember) {
        super(parent.getServices());
        this.parent = parent;
        this.directoryMember = directoryMember;
    }
        
    
    @Override
    public Date getCreateDate() {
        return directoryMember.getMemberItem().getItem().getCreateDate();
    }

    @Override
    public String getName() {
        return directoryMember.getName();
    }

    @Override
    public Date getModifiedDate() {
        return directoryMember.getMemberItem().getModifiedDate();
    }

    @Override
    public VersionCollectionResource getParent() {
        return parent;
    }

    @Override
    public BaseEntity getOwner() {
        return parent.getOwner();
    }

    @Override
    public void addPrivs(List<Priviledge> list, User user) {
        ItemVersion itemVersion = directoryMember.getMemberItem();
        if( itemVersion != null ) {
            List<Permission> perms = itemVersion.getItem().grantedPermissions(user);
            SecurityUtils.addPermissions(perms, list);
        }
        getParent().addPrivs(list, user);
    }    
    
}
