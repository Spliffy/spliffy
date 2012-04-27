package org.spliffy.server.web;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.http11.auth.DigestResponse;
import org.spliffy.server.db.BaseEntity;
import org.spliffy.server.db.User;
import org.spliffy.server.db.UserDao;

/**
 *
 * @author brad
 */
public class SpliffySecurityManager {

    private String realm = "spliffy";
    private final UserDao userDao;
    private final PasswordManager passwordManager;

    public SpliffySecurityManager(UserDao userDao, PasswordManager passwordManager) {
        this.userDao = userDao;
        this.passwordManager = passwordManager;
    }

    public Object authenticate(String userName, String requestPassword) {
        System.out.println("Basic authentication: " + userName + "/" + requestPassword);
        User user = userDao.getUser(userName);
        if (user == null) {
            return null;
        } else {
            // only the password hash is stored on the user, so need to generate an expected hash
            String requestedDigest = passwordManager.calcPasswordHash(userName, requestPassword);
            if (requestedDigest.equals(user.getPasswordDigest())) {
                return user;
            } else {
                System.out.println("password digests do not match");
                System.out.println(requestedDigest + " != " + user.getPasswordDigest());
                return null;
            }
        }
    }

    public Object authenticate(DigestResponse digest) {
        User user = userDao.getUser(digest.getUser());
        if (user == null) {
            return null;
        }
        if (passwordManager.verifyDigest(digest, user)) {
            return user;
        } else {
            return null;
        }
    }

    public String getRealm() {
        return realm;
    }

    public boolean authorise(Request rqst, Method method, Auth auth, Resource aThis) {
        if (aThis instanceof SpliffyResource) {
            SpliffyResource sr = (SpliffyResource) aThis;
            BaseEntity owner = sr.getOwner();
            if (owner == null) {
                return true;
            } else {
                // check owner matches current user
                if (auth != null && auth.getTag() != null) {
                    User user = (User) auth.getTag();
                    return owner.containsUser(user);
                } else {
                    return false;
                }
            }
        } else {
            return true;
        }
    }
}
