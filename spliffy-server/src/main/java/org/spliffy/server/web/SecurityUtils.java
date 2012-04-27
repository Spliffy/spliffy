package org.spliffy.server.web;

import com.ettrema.http.AccessControlledResource;
import com.ettrema.http.AccessControlledResource.Priviledge;
import com.ettrema.http.acl.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.spliffy.server.db.BaseEntity;
import org.spliffy.server.db.Permission;

/**
 *
 * @author brad
 */
public class SecurityUtils {
    public static Map<Principal, List<AccessControlledResource.Priviledge>> toMap(List<Permission> perms) {
        Map<Principal, List<AccessControlledResource.Priviledge>> map = new HashMap<>();
        if (perms != null) {
            for (Permission p : perms) {
                BaseEntity grantee = p.getGrantee();
                Principal principal = SpliffyResourceFactory.getRootFolder().findEntity(grantee.getName());
                List<AccessControlledResource.Priviledge> list = map.get(principal);
                if (list == null) {
                    list = new ArrayList<>();
                    map.put(principal, list);
                }
                list.add(p.getPriviledge());
            }
        }
        return map;
    }

    public static void addPermissions(List<Permission> perms, List<Priviledge> list) {
        for( Permission p : perms) {
            list.add(p.getPriviledge());
        }
    }

    public static boolean hasWrite(List<Priviledge> privs) {
        for( Priviledge p : privs ) {
            if( p.equals(Priviledge.WRITE)) {
                return true;
            }
        }
        return false;
    }

    static boolean hasRead(List<Priviledge> privs) {
        for( Priviledge p : privs ) {
            if( p.equals(Priviledge.READ)) {
                return true;
            }
        }
        return false;        
    }
}
