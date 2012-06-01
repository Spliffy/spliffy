package org.spliffy.server.web.sharing;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.ettrema.http.acl.Principal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.spliffy.server.db.BaseEntity;
import org.spliffy.server.db.Organisation;
import org.spliffy.server.db.Share;
import org.spliffy.server.db.utils.SessionManager;
import org.spliffy.server.db.Profile;
import org.spliffy.server.web.AbstractResource;
import org.spliffy.server.web.SpliffyCollectionResource;

/**
 *
 * @author brad
 */
public class SharesFolder extends AbstractResource implements CollectionResource{

    private final String name;
    
    private final SpliffyCollectionResource parent;

    public SharesFolder(String name, SpliffyCollectionResource parent) {
        super(parent.getServices());
        this.name = name;
        this.parent = parent;
    }


    @Override
    public boolean isDir() {
        return false;
    }

    @Override
    public SpliffyCollectionResource getParent() {
        return parent;
    }

    @Override
    public BaseEntity getOwner() {
        return null;
    }

    @Override
    public void addPrivs(List<Priviledge> list, Profile user) {
        
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Date getModifiedDate() {
        return null;
    }

    @Override
    public Date getCreateDate() {
        return null;
    }

    @Override
    public Resource child(String childName) throws NotAuthorizedException, BadRequestException {
        UUID id;
        try {
            id = UUID.fromString(childName);
        } catch (Exception e) {
            return null; // not a UUID
        }
        Share link = Share.get(id, SessionManager.session());
        if( link == null) {
            return null;
        }
        return new ShareResource(link, parent);
    }

    @Override
    public List<? extends Resource> getChildren() throws NotAuthorizedException, BadRequestException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Map<Principal, List<Priviledge>> getAccessControlList() {
        return null;
    }

    @Override
    public Organisation getOrganisation() {
        return parent.getOrganisation();
    }
    

    
}
