package org.spliffy.server.web;

import com.bradmcevoy.http.*;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.exceptions.NotFoundException;
import com.bradmcevoy.http.values.HrefList;
import com.ettrema.http.AccessControlledResource;
import com.ettrema.http.acl.HrefPrincipleId;
import com.ettrema.http.acl.Principal;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import org.hibernate.Transaction;
import org.spliffy.server.db.*;
import org.spliffy.server.web.versions.VersionsRootFolder;

/**
 *
 * @author brad
 */
public class UserResource extends AbstractCollectionResource implements CollectionResource, MakeCollectionableResource, PropFindableResource, GetableResource, PrincipalResource {

    public static final String ADDRESS_BOOK_HOME_NAME = "abs";
    public static final String CALENDAR_HOME_NAME = "cal";
    
    private final User user; 
    private final SpliffyCollectionResource parent;
    private final VersionNumberGenerator versionNumberGenerator;
    private List<Resource> children;

    public UserResource(SpliffyCollectionResource parent, User u, VersionNumberGenerator versionNumberGenerator) {
        super(parent.getServices());
        this.parent = parent;
        this.user = u;
        this.versionNumberGenerator = versionNumberGenerator;

    }

    @Override
    public Resource child(String childName) throws NotAuthorizedException, BadRequestException {
        switch (childName) {
            case CALENDAR_HOME_NAME:
                break;
            case ADDRESS_BOOK_HOME_NAME:
                break;
        }
        return Utils.childOf(getChildren(), childName);
    }

    @Override
    public List<? extends Resource> getChildren() throws NotAuthorizedException, BadRequestException {
        if (children == null) {
            children = new ArrayList();
            if (user.getRepositories() != null) {
                for (Repository r : user.getRepositories()) {
                    RepoVersion rv = r.latestVersion();
                    if (rv != null) {
                        System.out.println("Using latest version: " + rv.getVersionNum());
                    }
                    RepositoryFolder rr = new RepositoryFolder(this, r, rv);
                    children.add(rr);
                }
            }
            // add the versions root, to allow browsing of old versions
            VersionsRootFolder versionsRoot = new VersionsRootFolder(this, user, services);
            children.add(versionsRoot);
        }
        return children;
    }

    @Override
    public String getName() {
        return user.getName();
    }

    @Override
    public CollectionResource createCollection(String newName) throws NotAuthorizedException, ConflictException, BadRequestException {
        Transaction tx = SessionManager.session().beginTransaction();
        Repository r = new Repository();
        r.setBaseEntity(user);
        r.setName(newName);
        r.setVersions(new ArrayList<RepoVersion>());
        r.setCreatedDate(new Date());
        List<Repository> list = user.getRepositories();
        if (list == null) {
            list = new ArrayList<>();
        }
        list.add(r);
        SessionManager.session().save(r);
        tx.commit();
        RepoVersion rv = r.latestVersion();
        return new RepositoryFolder(this, r, rv);
    }


    @Override
    public Date getCreateDate() {
        return user.getCreatedDate();
    }

    @Override
    public Date getModifiedDate() {
        return user.getModifiedDate();
    }

    @Override
    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException, NotFoundException {
        getTemplater().writePage("userHome.ftl", this, params, out);
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

    @Override
    public SpliffyCollectionResource getParent() {
        return parent;
    }

    @Override
    public BaseEntity getOwner() {
        return user;
    }

    public String getHref() {
        return "/" + getName() + "/";
    }
    
    @Override
    public PrincipleId getIdenitifer() {
        return new HrefPrincipleId(getHref());
    }

    @Override
    public HrefList getCalendarHomeSet() {
        return HrefList.asList(getHref() + "calendars/");
    }

    @Override
    public HrefList getCalendarUserAddressSet() {
        return HrefList.asList(getHref());
    }

    @Override
    public String getScheduleInboxUrl() {
        return null;
    }

    @Override
    public String getScheduleOutboxUrl() {
        return null;
    }

    @Override
    public String getDropBoxUrl() {
        return null;
    }

    @Override
    public HrefList getAddressBookHomeSet() {
        return HrefList.asList(getHref() + "abs/"); // the address books folder
    }

    @Override
    public String getAddress() {
        return getHref() + "abs/";
    }

    @Override
    public void addPrivs(List<Priviledge> list, User u) {
        // Give this user special permissions
        if( user.getName().equals(u.getName())) {
            list.add(Priviledge.READ);
            list.add(Priviledge.WRITE);
            list.add(Priviledge.READ_ACL);
            list.add(Priviledge.UNLOCK);
            list.add(Priviledge.WRITE_CONTENT);
            list.add(Priviledge.WRITE_PROPERTIES);
        }
    }

    /**
     * Get all allowed priviledges for all principals on this resource. Note
     * that a principal might be a user, a group, or a built-in webdav group
     * such as AUTHENTICATED
     *
     * @return
     */
    @Override
    public Map<Principal, List<AccessControlledResource.Priviledge>> getAccessControlList() {
        Map<Principal, List<AccessControlledResource.Priviledge>> map = new HashMap<>();
        List<Priviledge> list = new ArrayList<>();
        addPrivs(list, user);
        map.put(this, list);
        return map;
    }        
    
}
