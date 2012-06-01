package org.spliffy.server.web;

import com.ettrema.http.AccessControlledResource;
import com.ettrema.http.AccessControlledResource.Priviledge;
import com.ettrema.http.acl.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.spliffy.server.db.BaseEntity;
import org.spliffy.server.db.Group;
import org.spliffy.server.db.GroupMembership;
import org.spliffy.server.db.Permission;

/**
 *
 * @author brad
 */
public class SecurityUtils {

    public static Set<Permission> getPermissions(BaseEntity grantee, BaseEntity grantedOn, Session session) {
        Set<Permission> perms = new HashSet<>();
        appendPermissions(grantee, grantedOn, session, perms);
        return perms;
    }

    private static void appendPermissions(BaseEntity grantee, BaseEntity grantedOn, Session session, Set<Permission> perms) {
        System.out.println("addPermissions: grantedTo: " + grantee.getName() + " grantedOn: " + grantedOn.getName());
        Criteria crit = session.createCriteria(Permission.class);
        crit.add(Expression.and(Expression.eq("grantee", grantee), Expression.eq("grantedOnEntity", grantedOn)));
        addPerms(perms, crit);

        List<GroupMembership> memberships = grantee.getMemberships();
        if (memberships != null) {
            for (GroupMembership m : memberships) {
                Group g = m.getGroupEntity();
                appendPermissions(g, grantedOn, session, perms);
            }
        }
    }

    private static void addPerms(Set<Permission> perms, Criteria crit) {
        List list = crit.list();
        if (list != null && !list.isEmpty()) {
            for (Object o : list) {
                perms.add((Permission) o);
            }
        }
    }

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

    public static void addPermissions(Collection<Permission> perms, List<Priviledge> list) {
        for (Permission p : perms) {
            list.add(p.getPriviledge());
        }
    }

    public static boolean hasWrite(List<Priviledge> privs) {
        for (Priviledge p : privs) {
            if (p.equals(Priviledge.WRITE)) {
                return true;
            }
        }
        return false;
    }

    static boolean hasRead(List<Priviledge> privs) {
        for (Priviledge p : privs) {
            if (p.equals(Priviledge.READ)) {
                return true;
            }
        }
        return false;
    }
}
