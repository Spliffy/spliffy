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

import java.io.Serializable;
import javax.persistence.*;

/**
 * A branch is a version of a repository which is mutable. Ie as changes are made
 * the version is updated.
 *
 * @author brad
 */
@Entity
public class Branch implements Serializable{
    
    /**
     * Special branch which always exists on a repository
     */
    public static String TRUNK = "trunk";
    
    private Long id;
    private String name;
    private Repository repository;
    private Commit head;
    private Branch linkedTo;

    @Id
    @GeneratedValue
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

        
    @ManyToOne(optional=true)
    public Commit getHead() {
        return head;
    }

    public void setHead(Commit head) {
        this.head = head;
    }
    
       
    /**
     * If set, then this repository is just a pointer to it
     * 
     * @return 
     */
    @ManyToOne
    public Branch getLinkedTo() {
        return linkedTo;
    }

    public void setLinkedTo(Branch linkedTo) {
        this.linkedTo = linkedTo;
    }

    @Column(nullable=false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    @ManyToOne(optional=false)    
    public Repository getRepository() {
        return repository;
    }
    
    
    
        
}
