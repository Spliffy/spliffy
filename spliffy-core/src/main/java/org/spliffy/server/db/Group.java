/*
 * Copyright (C) 2012 McEvoy Software Ltd
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.spliffy.server.db;

import com.ettrema.http.AccessControlledResource.Priviledge;
import java.util.List;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import org.hibernate.Criteria;
import org.hibernate.criterion.Expression;
import org.spliffy.server.db.utils.SessionManager;

/**
 * A user group, is a list of users and other groups. Is typically used to convey priviledges
 * to a selected set of users.
 * 
 * A group is defined within an organisation and can only convey privs within that
 * organisation, although that could be passed down to child organisations
 *
 * @author brad
 */
@javax.persistence.Entity
@Table(name="GROUP_ENTITY")
@DiscriminatorValue("G")
public class Group extends BaseEntity {
    
    
    public static String ADMINISTRATORS = "administrators";
    public static String USERS = "users";
    
    public boolean isMember(BaseEntity u) {
        Criteria crit = SessionManager.session().createCriteria(GroupMembership.class);
        List list = crit.add(Expression.and(Expression.eq("member", u), Expression.eq("groupEntity", this))).list();
        return list != null && !list.isEmpty();
    }    
    
}
