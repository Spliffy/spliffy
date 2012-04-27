package org.spliffy.server.web.calendar;

import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import java.util.Date;
import java.util.List;
import org.spliffy.server.db.BaseEntity;
import org.spliffy.server.db.ItemVersion;
import org.spliffy.server.db.User;
import org.spliffy.server.web.AbstractCollectionResource;
import org.spliffy.server.web.Services;
import org.spliffy.server.web.SpliffyCollectionResource;
import org.spliffy.server.web.UserResource;
import org.spliffy.server.web.Utils;

/**
 *
 * @author brad
 */
public class CalendarHomeFolder extends AbstractCollectionResource {
    private final String name;
    private final UserResource parent;

    public CalendarHomeFolder(UserResource parent, Services services, String name) {
        super(services);
        this.parent = parent;
        this.name = name;
    }

    @Override
    public ItemVersion getItemVersion() {
        return null;
    }

    @Override
    public SpliffyCollectionResource getParent() {
        return parent;
    }

    @Override
    public BaseEntity getOwner() {
        return parent.getOwner();
    }

    @Override
    public void addPrivs(List<Priviledge> list, User user) {
        parent.addPrivs(list, user);
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
        return Utils.childOf(getChildren(), childName);
    }

    @Override
    public List<? extends Resource> getChildren() throws NotAuthorizedException, BadRequestException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    
    
}
