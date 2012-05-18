package org.spliffy.server.web;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.Cookie;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.http11.auth.DigestResponse;
import com.ettrema.http.AccessControlledResource;
import com.ettrema.http.AccessControlledResource.Priviledge;
import java.util.List;
import java.util.Map.Entry;
import org.spliffy.server.db.User;
import org.spliffy.server.db.utils.UserDao;

/**
 *
 * @author brad
 */
public class SpliffySecurityManager {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SpliffySecurityManager.class);
    private String realm = "spliffy";
    private final UserDao userDao;
    private final PasswordManager passwordManager;

    public SpliffySecurityManager(UserDao userDao, PasswordManager passwordManager) {
        this.userDao = userDao;
        this.passwordManager = passwordManager;
    }

    public User authenticate(String userName, String requestPassword) {
        User user = userDao.getUser(userName);
        if (user == null) {
            return null;
        } else {
            // only the password hash is stored on the user, so need to generate an expected hash
            if (passwordManager.verifyPassword(user, requestPassword)) {
                return user;
            } else {
                return null;
            }
        }
    }

    public User authenticate(DigestResponse digest) {
        log.info("authenticate: " + digest.getUser());
        User user = userDao.getUser(digest.getUser());
        if (user == null) {
            log.warn("user not found: " + digest.getUser());
            return null;
        }
        if (passwordManager.verifyDigest(digest, user)) {
            log.warn("digest auth ok: " + user.getName());
            return user;
        } else {
            log.warn("password verifuication failed");
            return null;
        }
    }

    public String getRealm() {
        return realm;
    }

    public boolean authorise(Request req, Method method, Auth auth, Resource aThis) {
        if (aThis instanceof AccessControlledResource) {
            AccessControlledResource acr = (AccessControlledResource) aThis;
            List<Priviledge> privs = acr.getPriviledges(auth);
            boolean result;
            if (method.isWrite) {
                result = SecurityUtils.hasWrite(privs);
            } else {
                result = SecurityUtils.hasRead(privs);
            }
            if (!result) {
                log.info("Denied access of: " + auth + " to resource: " + aThis.getName() + " (" + aThis.getClass() + ")");
                log.info("Allowed privs are:");
                for (Priviledge p : privs) {
                    log.info("   - " + p);
                }
            }
            return result;
        } else {
            return true; // not access controlled so must be ok!
        }
    }

    public PasswordManager getPasswordManager() {
        return passwordManager;
    }

    public UserDao getUserDao() {
        return userDao;
    }
}
