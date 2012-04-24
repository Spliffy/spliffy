package org.spliffy.server.web;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import org.spliffy.server.web.sync.BaseResource;

/**
 *
 * @author brad
 */
public class SpliffySecurityManager {
    
    private String realm = "spliffy";
    
    public Object authenticate(String user, String password) {
        return user;
    }
    
    public String getRealm() {
        return realm;
    }

    public boolean authorise(Request rqst, Method method, Auth auth, BaseResource aThis) {
        return true;
    }
}
