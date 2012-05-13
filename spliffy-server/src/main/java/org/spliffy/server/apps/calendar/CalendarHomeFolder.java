package org.spliffy.server.apps.calendar;

import com.bradmcevoy.http.*;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.exceptions.NotFoundException;
import com.ettrema.http.acl.Principal;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.spliffy.server.db.BaseEntity;
import org.spliffy.server.db.Calendar;
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
public class CalendarHomeFolder extends AbstractCollectionResource implements MakeCollectionableResource, GetableResource {

    private final String name;
    private final UserResource parent;
    private final CalendarManager calendarManager;
    private List<CalendarFolder> children;

    public CalendarHomeFolder(UserResource parent, Services services, String name, CalendarManager calendarManager) {
        super(services);
        this.parent = parent;
        this.name = name;
        this.calendarManager = calendarManager;
    }

    public String getHref() {
        return parent.getHref() + name + "/";
    }    
    
    @Override
    public CollectionResource createCollection(String newName) throws NotAuthorizedException, ConflictException, BadRequestException {
        Calendar calendar = calendarManager.createCalendar(parent.getOwner(), newName);
        return new CalendarFolder(this, services, calendar, calendarManager);
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
        if (children == null) {
            List<Calendar> calendarList = this.getOwner().getCalendars();
            children = new ArrayList<>();
            if (calendarList != null) {
                for (Calendar cal : calendarList) {
                    CalendarFolder f = new CalendarFolder(this, services, cal, calendarManager);
                    children.add(f);
                }
            }
        }
        return children;
    }

    @Override
    public Map<Principal, List<Priviledge>> getAccessControlList() {
        return null;
    }

    @Override
    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException, NotFoundException {
        getServices().getTemplater().writePage("calendarsHome.ftl", this, params, out, currentUser);
    }

    @Override
    public Long getMaxAgeSeconds(Auth auth) {
        return null;
    }

    @Override
    public String getContentType(String accepts) {
        return "text/html";
    }

    @Override
    public Long getContentLength() {
        return null;
    }


}
