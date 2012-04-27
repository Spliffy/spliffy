package org.spliffy.server.web;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.http11.auth.DigestResponse;
import org.hashsplit4j.api.BlobStore;
import org.hashsplit4j.api.HashStore;
import org.spliffy.server.db.ItemVersion;
import org.spliffy.server.db.User;

/**
 *
 * @author brad
 */
public abstract class AbstractResource implements SpliffyResource,PropFindableResource {

    
    public abstract ItemVersion getItemVersion();
    
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
        return services.getSecurityManager().authorise(request, method, auth, this);
    }

    @Override
    public String getRealm() {
        return "spliffy";
    }

    /**
     * Check for correctly formed folder paths on GET requests
     * 
     * If request is a GET, and the resource is a collection, then if
     * the url does NOT end with a slash redirect to ../
     * 
     * @param request
     * @return 
     */
    @Override
    public String checkRedirect(Request request) {
        if( request.getMethod().equals(Request.Method.GET)) {
            if( this instanceof CollectionResource) {
                String url = request.getAbsolutePath();
                if( !url.endsWith("/")) {
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

    
    
}
