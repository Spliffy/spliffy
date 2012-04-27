package org.spliffy.server.web.versions;

import java.util.Date;
import org.spliffy.server.db.BaseEntity;
import org.spliffy.server.db.DirectoryMember;
import org.spliffy.server.web.AbstractResource;

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

    
}
