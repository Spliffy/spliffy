package org.spliffy.server.web;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.http11.auth.DigestResponse;
import com.bradmcevoy.http.values.HrefList;
import com.ettrema.http.AccessControlledResource;
import com.ettrema.http.acl.Principal;
import com.ettrema.logging.LogUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.hashsplit4j.api.BlobStore;
import org.hashsplit4j.api.HashStore;
import org.spliffy.server.db.BaseEntity;
import org.spliffy.server.db.User;

/**
 *
 * @author brad
 */
public abstract class AbstractResource implements SpliffyResource, PropFindableResource, AccessControlledResource {
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractResource.class);

    /**
     * For templating, return true if this is a directory, false for a file
     */
    public abstract boolean isDir();
    protected final Services services;
    protected User currentUser;

    public AbstractResource(Services services) {
        this.services = services;
    }

    @Override
    public String getUniqueId() {
        return null;
    }

    @Override
    public Object authenticate(String user, String password) {
        currentUser = (User) services.getSecurityManager().authenticate(user, password);
        return currentUser;
    }

    @Override
    public Object authenticate(DigestResponse digestRequest) {
        currentUser = (User) services.getSecurityManager().authenticate(digestRequest);
        return currentUser;
    }

    @Override
    public boolean authorise(Request request, Method method, Auth auth) {
        boolean b = services.getSecurityManager().authorise(request, method, auth, this);
        if( !b ) {
            LogUtils.info(log, "authorisation failed", auth,"resource:", getName(), "method:", method);
        }
        return b;
    }

    @Override
    public String getRealm() {
        return "spliffy";
    }

    /**
     * Check for correctly formed folder paths on GET requests
     *
     * If request is a GET, and the resource is a collection, then if the url
     * does NOT end with a slash redirect to ../
     *
     * @param request
     * @return
     */
    @Override
    public String checkRedirect(Request request) {
        if (request.getMethod().equals(Request.Method.GET)) {
            if (this instanceof CollectionResource) {
                String url = request.getAbsolutePath();
                if (!url.endsWith("/")) {
                    return url + "/";
                }
            }
        }
        return null;
    }

    public BlobStore getBlobStore() {
        return services.getBlobStore();
    }

    public HashStore getHashStore() {
        return services.getHashStore();
    }

    public Templater getTemplater() {
        return services.getTemplater();
    }

    @Override
    public Services getServices() {
        return services;
    }

    @Override
    public boolean isDigestAllowed() {
        return true;
    }

    @Override
    public User getCurrentUser() {
        return currentUser;
    }
    

    @Override
    public String getPrincipalURL() {
        BaseEntity entity = getOwner();
        if (entity == null) {
            return null;
        } else {
            return "/" + entity.getName(); // probably would be good to put this into a UrlMapper interface
        }
    }

    /**
     * Return the list of privlidges which the current user (given by the auth
     * object) has access to, on this resource.
     *
     * @param auth
     * @return
     */
    @Override
    public List<AccessControlledResource.Priviledge> getPriviledges(Auth auth) {
        List<AccessControlledResource.Priviledge> list = new ArrayList<>();
        if (auth != null && auth.getTag() != null) {
            User user = (User) auth.getTag();
            addPrivs(list, user);
        }
        return list;
    }
    


    @Override
    public void setAccessControlList(Map<Principal, List<Priviledge>> privs) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    

    /**
     * Return the hrefs (either fully qualified URLs or absolute paths) to the
     * collections which contain principals. This is to allow user agents to
     * display a list of users to display.
     *
     * Most implementations will only have a single value which will be the path
     * to the users folder. Eg:
     *
     * return Arrays.asList("/users/");
     *
     * @return - a list of hrefs
     */
    @Override
    public HrefList getPrincipalCollectionHrefs() {
        HrefList list = new HrefList();
        list.add("/users/");
        return list;
    }
    
}
