package org.spliffy.server.web;

import com.bradmcevoy.http.http11.auth.DigestGenerator;
import com.bradmcevoy.http.http11.auth.DigestResponse;
import org.spliffy.server.db.User;

/**
 *
 * @author brad
 */
public class PasswordManager {
    
    private final DigestGenerator digestGenerator;
            
    private String realm = "spliffy";

    public PasswordManager(DigestGenerator digestGenerator) {
        this.digestGenerator = digestGenerator;
    }
            
    public PasswordManager() {
        this.digestGenerator = new DigestGenerator();
    }
                
    
    public void setPassword(User user, String newPassword) {
        String a1md5 = calcPasswordHash(user.getName(), newPassword);
        user.setPasswordDigest(a1md5);
    }
    
    public String calcPasswordHash(String userName, String password) {
        String a1md5 = digestGenerator.encodePasswordInA1Format(userName, realm, password);
        System.out.println("calcPasswordHash: " + userName + "/" + realm + "/" + password + " = " + a1md5);
        return a1md5;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }
    
    public boolean verifyDigest(DigestResponse digest, User user) {        
        String a1Md5 = user.getPasswordDigest();
        String expectedResp = digestGenerator.generateDigestWithEncryptedPassword(digest, a1Md5);
        String actualResp = digest.getResponseDigest();
        System.out.println("VerifyDigest: " + expectedResp + " - " + actualResp);
        if( expectedResp.equals(actualResp)) {
            System.out.println("ok");
            return true;
        } else {
            System.out.println("digests don't match");
            return false;
        }
        
    }
    
}
