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

import java.util.Date;
import javax.persistence.*;

/**
 *
 * @author brad
 */
@Entity
public class GroupMembership {
    private Long id;
    private BaseEntity member;
    private Group group;
    private Date createdDate;
    private Date modifiedDate;
    
    @Id
    @GeneratedValue
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ManyToOne(optional=false)
    public BaseEntity getMember() {
        return member;
    }

    public void setMember(BaseEntity member) {
        this.member = member;
    }

    @ManyToOne(optional=false)
    public Group getGroupEntity() {
        return group;
    }

    public void setGroupEntity(Group group) {
        this.group = group;
    }
    
    
    
    @Column(nullable=false)
    @Temporal(javax.persistence.TemporalType.DATE)
    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    @Column(nullable=false)
    @Temporal(javax.persistence.TemporalType.DATE)
    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }
    
}
