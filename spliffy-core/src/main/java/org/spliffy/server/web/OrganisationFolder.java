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
package org.spliffy.server.web;

import com.bradmcevoy.http.*;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.exceptions.NotFoundException;
import com.ettrema.http.acl.Principal;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import org.hibernate.Session;
import org.spliffy.server.apps.ApplicationManager;
import org.spliffy.server.db.BaseEntity;
import org.spliffy.server.db.Organisation;
import org.spliffy.server.db.Permission;
import org.spliffy.server.db.Profile;
import org.spliffy.server.db.utils.SessionManager;

/**
 * This is the root folder for the admin site. The admin site is used to setup
 * users and websites accessing the server
 *
 * @author brad
 */
public class OrganisationFolder extends AbstractResource implements RootFolder, SpliffyCollectionResource, GetableResource, PropFindableResource {

    private Map<String, PrincipalResource> children = new HashMap<>();
    private final ApplicationManager applicationManager;
    private final Organisation organisation;

    public OrganisationFolder(Services services, ApplicationManager applicationManager, Organisation organisation) {
        super(services);
        this.applicationManager = applicationManager;
        this.organisation = organisation;
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public boolean authorise(Request request, Request.Method method, Auth auth) {
        if (method.equals(Method.PROPFIND)) { // force login for webdav browsing
            return currentUser != null;
        }
        return true;
    }

    @Override
    public Date getModifiedDate() {
        return null;
    }

    @Override
    public Resource child(String childName) throws NotAuthorizedException, BadRequestException {
        Resource r = applicationManager.getPage(this, childName);
        if (r != null) {
            return r;
        }
        return findEntity(childName);
    }

    @Override
    public PrincipalResource findEntity(String name) {
        PrincipalResource r = children.get(name);
        if (r != null) {
            return r;
        }
        Session session = SessionManager.session();
        Profile u = services.getSecurityManager().getUserDao().getProfile(name, organisation, session);
        if (u == null) {
            return null;
        } else {
            UserResource ur = new UserResource(this, u, applicationManager);
            children.put(name, ur);
            return ur;
        }
    }

    @Override
    public List<? extends Resource> getChildren() throws NotAuthorizedException, BadRequestException {
        if (currentUser == null) {
            throw new NotAuthorizedException("Need to be logged in to browse");
        }
        PrincipalResource r = findEntity(currentUser.getName());
        List<Resource> list = new ArrayList<>();
        list.add(r);
        return list;
    }

    @Override
    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException, NotFoundException {
        services.getTemplater().writePage("home.ftl", this, params, out, getCurrentUser());
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
    public SpliffyCollectionResource getParent() {
        return null;
    }

    @Override
    public BaseEntity getOwner() {
        return null;
    }

    @Override
    public void addPrivs(List<Priviledge> list, Profile user) {
        // get permissions of this user on the organisation
        Set<Permission> perms = SecurityUtils.getPermissions(user, organisation, SessionManager.session());
        System.out.println("AdminRootFolder: addPRivs: " + perms);
        SecurityUtils.addPermissions(perms, list); 
    }

    @Override
    public Date getCreateDate() {
        return null;
    }

    @Override
    public boolean isDir() {
        return true;
    }

    @Override
    public Map<Principal, List<Priviledge>> getAccessControlList() {
        return Collections.EMPTY_MAP;
    }

    @Override
    public Organisation getOrganisation() {
        return organisation;
    }
    
    
}
