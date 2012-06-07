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

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.*;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.exceptions.NotFoundException;
import com.ettrema.http.acl.Principal;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.spliffy.server.db.*;
import org.spliffy.server.web.templating.HtmlPage;
import org.spliffy.server.web.templating.WebResource;

/**
 *
 * @author brad
 */
public class RenderFileResource extends AbstractResource implements MutableResource, GetableResource, MoveableResource, CopyableResource, DeletableResource, HtmlPage {

    private final FileResource fileResource;
    
    private final List<String> bodyClasses = new ArrayList<>();
    private final List<WebResource> webResources = new ArrayList<>();
    
    private String title;
    private String body;

    public RenderFileResource(Services services, FileResource fileResource) {
        super(services);
        this.fileResource = fileResource;
    }

    @Override
    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException, NotFoundException {
        services.getTemplateParser().parse(this, Path.root);
        System.out.println(body);
        
        services.getHtmlTemplater().writePage("content/page", this, params, out);
    }

    @Override
    public InputStream getInputStream() {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try {
            fileResource.sendContent(bout, null, null, title);
        } catch (IOException | NotAuthorizedException | BadRequestException | NotFoundException ex) {
            throw new RuntimeException(ex);
        }
        return new ByteArrayInputStream(bout.toByteArray());
    }    
    
    @Override
    public List<String> getBodyClasses() {
        return bodyClasses;
    }

    @Override
    public List<WebResource> getWebResources() {
        return webResources;
    }

    @Override
    public String getBody() {
        return body;
    }    
    
    
    @Override
    public Long getContentLength() {
        return null;
    }
    
    
    @Override
    public boolean isDir() {
        return false;
    }

    @Override
    public SpliffyCollectionResource getParent() {
        return fileResource.getParent();
    }

    @Override
    public BaseEntity getOwner() {
        return fileResource.getOwner();
    }

    @Override
    public Organisation getOrganisation() {
        return fileResource.getOrganisation();
    }

    @Override
    public void addPrivs(List<Priviledge> list, Profile user) {
        fileResource.addPrivs(list, user);
    }

    @Override
    public String getName() {
        return fileResource.getName();
    }

    @Override
    public Date getModifiedDate() {
        return fileResource.getModifiedDate();
    }

    @Override
    public Date getCreateDate() {
        return fileResource.getCreateDate();
    }

    @Override
    public Map<Principal, List<Priviledge>> getAccessControlList() {
        return fileResource.getAccessControlList();
    }

    @Override
    public boolean isDirty() {
        return fileResource.isDirty();
    }

    @Override
    public Long getEntryHash() {
        return fileResource.getEntryHash();
    }

    @Override
    public ItemVersion getItemVersion() {
        return fileResource.getItemVersion();
    }

    @Override
    public void setItemVersion(ItemVersion newVersion) {
        fileResource.setItemVersion(newVersion);
    }

    @Override
    public String getType() {
        return fileResource.getType();
    }

    @Override
    public DirectoryMember getDirectoryMember() {
        return fileResource.getDirectoryMember();
    }


    @Override
    public Long getMaxAgeSeconds(Auth auth) {
        return null;
    }

    @Override
    public String getContentType(String accepts) {
        return fileResource.getContentType(accepts);
    }

    @Override
    public void moveTo(CollectionResource rDest, String name) throws ConflictException, NotAuthorizedException, BadRequestException {
        fileResource.moveTo(rDest, name);
    }

    @Override
    public void copyTo(CollectionResource toCollection, String name) throws NotAuthorizedException, BadRequestException, ConflictException {
        fileResource.copyTo(toCollection, name);
    }

    @Override
    public void delete() throws NotAuthorizedException, ConflictException, BadRequestException {
        fileResource.delete();
    }

    @Override
    public void setBody(String b) {
        this.body = b;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String t) {
        this.title = t;
    }
}
