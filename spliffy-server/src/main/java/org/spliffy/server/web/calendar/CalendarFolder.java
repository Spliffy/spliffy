package org.spliffy.server.web.calendar;

import com.bradmcevoy.http.ReportableResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.ettrema.http.CalendarResource;
import java.util.Date;
import java.util.List;
import org.spliffy.server.db.BaseEntity;
import org.spliffy.server.db.ItemVersion;
import org.spliffy.server.db.User;
import org.spliffy.server.web.AbstractCollectionResource;
import org.spliffy.server.web.Services;
import org.spliffy.server.web.SpliffyCollectionResource;

/**
 *
 * @author brad
 */
public class CalendarFolder extends AbstractCollectionResource implements CalendarResource, ReportableResource {
    private final CalendarHomeFolder parent;
    private final String name;

    public CalendarFolder(CalendarHomeFolder parent, Services services, String name) {
        super(services);
        this.parent = parent;
        this.name = name;
    }

    @Override
    public ItemVersion getItemVersion() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SpliffyCollectionResource getParent() {
        return parent;
    }

    @Override
    public BaseEntity getOwner() {
        return getParent().getOwner();
    }

    @Override
    public void addPrivs(List<Priviledge> list, User user) {
        getParent().addPrivs(list, user);
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
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<? extends Resource> getChildren() throws NotAuthorizedException, BadRequestException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getCalendarDescription() {
        return getName();
    }

    @Override
    public String getColor() {
        return "#2952A3";
    }

    @Override
    public void setColor(String s) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getCTag() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
