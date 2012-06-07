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

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.exceptions.NotFoundException;
import com.ettrema.http.acl.Principal;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.spliffy.server.db.BaseEntity;
import org.spliffy.server.db.Organisation;
import org.spliffy.server.db.Profile;

/**
 *
 * @author brad
 */
public class TemplatedHtmlPage extends AbstractResource implements GetableResource {

    protected final String name;
    protected final SpliffyCollectionResource parent;
    protected final String template;
    protected Map<String,Object> model;
    
    private boolean forceLogin;
    
    public TemplatedHtmlPage(String name, SpliffyCollectionResource parent, Services services, String template) {
        super(services);
        this.name = name;
        this.parent = parent;       
        this.template = template;
    }

    @Override
    public boolean authorise(Request request, Method method, Auth auth) {
        if( forceLogin ) {
            if( auth == null || auth.getTag() == null ) {
                return false;
            }
        }
        return true;
    }

    public boolean isForceLogin() {
        return forceLogin;
    }

    public void setForceLogin(boolean forceLogin) {
        this.forceLogin = forceLogin;
    }

    
    
    @Override
    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException, NotFoundException {
        model = buildModel(params);
        services.getHtmlTemplater().writePage(template, this, params, out);
    }
       
    @Override
    public Long getContentLength() {
        return null;
    }

    @Override
    public String getContentType(String accepts) {
        return "text/html";
    }
                       
    @Override
    public boolean isDir() {
        return false;
    }

    @Override
    public SpliffyCollectionResource getParent() {
        return parent;
    }

    @Override
    public BaseEntity getOwner() {
        return parent.getOwner();
    }

    @Override
    public Organisation getOrganisation() {
        return parent.getOrganisation();
    }

    @Override
    public void addPrivs(List<Priviledge> list, Profile user) {
        parent.addPrivs(list, user);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Date getModifiedDate() {
        return parent.getModifiedDate();
    }

    @Override
    public Date getCreateDate() {
        return null;
    }

    @Override
    public Map<Principal, List<Priviledge>> getAccessControlList() {
        return null;
    }

    @Override
    public Long getMaxAgeSeconds(Auth auth) {
        return null;
    }

    public Map<String, Object> getModel() {
        return model;
    }

    /*
     * Override this to build and populate a map which will be available during
     * template execution
     */
    protected Map<String, Object> buildModel(Map<String, String> params) {
        return new HashMap<>();
    }
    
    
    
}
