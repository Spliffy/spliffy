package org.spliffy.server.web;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.exceptions.NotFoundException;
import com.bradmcevoy.http.http11.auth.DigestResponse;
import com.ettrema.http.AccessControlledResource.Priviledge;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.spliffy.server.db.BaseEntity;
import org.spliffy.server.db.User;

/**
 *
 * @author brad
 */
public class LoginPage implements GetableResource, SpliffyResource {
    private final SpliffySecurityManager securityManager;
    private final SpliffyCollectionResource parent;

    public LoginPage(SpliffySecurityManager securityManager, SpliffyCollectionResource parent) {
        this.securityManager = securityManager;
        this.parent = parent;
    }


    @Override
    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException, NotFoundException {
        parent.getServices().getTemplater().writePage("login", this, params, out); 
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
    public String getUniqueId() {
        return null;
    }

    @Override
    public String getName() {
        return "login";
    }

    @Override
    public Object authenticate(String user, String password) {
        return securityManager.authenticate(user, password);
    }
    
    @Override
    public Object authenticate(DigestResponse digestRequest) {
        return securityManager.authenticate(digestRequest);
    }    

    @Override
    public boolean authorise(Request request, Method method, Auth auth) {
        return auth != null; // this is just to force authentication
    }

    @Override
    public String getRealm() {
        return securityManager.getRealm();
    }

    @Override
    public Date getModifiedDate() {
        return null;
    }

    @Override
    public String checkRedirect(Request request) {
        if( request.getAuthorization() != null && request.getAuthorization().getTag() != null ) {
            // logged in, so go to user's home page
            User user = (User) request.getAuthorization().getTag();
            return "/" + user.getName() + "/";
        } else {
            return null;
        }
    }

    @Override
    public SpliffyCollectionResource getParent() {
        return parent;
    }

    @Override
    public Services getServices() {
        return parent.getServices();
    }


    @Override
    public boolean isDigestAllowed() {
        return true;
    }

    @Override
    public BaseEntity getOwner() {
        return null;
    }

    @Override
    public User getCurrentUser() {
        return null;
    }

    @Override
    public void addPrivs(List<Priviledge> list, User user) {

    }
    
    
}
