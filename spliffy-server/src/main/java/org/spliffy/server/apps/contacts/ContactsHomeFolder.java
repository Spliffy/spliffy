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
package org.spliffy.server.apps.contacts;

import com.bradmcevoy.http.*;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.exceptions.NotFoundException;
import com.ettrema.http.AccessControlledResource;
import com.ettrema.http.acl.Principal;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.spliffy.server.db.AddressBook;
import org.spliffy.server.db.BaseEntity;
import org.spliffy.server.db.User;
import org.spliffy.server.web.*;
import org.spliffy.server.web.Utils;

/**
 *
 * @author brad
 */
public class ContactsHomeFolder  extends AbstractCollectionResource implements MakeCollectionableResource, GetableResource {

    private final String name;
    private final UserResource parent;
    private final ContactManager contactManager;
    private List<ContactsFolder> children;

    public ContactsHomeFolder(UserResource parent, Services services, String name, ContactManager contactManager) {
        super(services);
        this.parent = parent;
        this.name = name;
        this.contactManager = contactManager;
    }

    @Override
    public CollectionResource createCollection(String newName) throws NotAuthorizedException, ConflictException, BadRequestException {
        AddressBook addressBook = contactManager.createAddressBook(parent.getOwner(), newName);
        return new ContactsFolder(this, services, addressBook, contactManager);
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
    public void addPrivs(List<AccessControlledResource.Priviledge> list, User user) {
        parent.addPrivs(list, user);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Date getModifiedDate() {
        return null;
    }

    @Override
    public Date getCreateDate() {
        return null;
    }

    @Override
    public Resource child(String childName) throws NotAuthorizedException, BadRequestException {
        return Utils.childOf(getChildren(), childName);
    }

    @Override
    public List<? extends Resource> getChildren() throws NotAuthorizedException, BadRequestException {
        if (children == null) {
            List<AddressBook> addressBooks = this.getOwner().getAddressBooks();
            children = new ArrayList<>();
            if (addressBooks != null) {
                for (AddressBook cal : addressBooks) {
                    ContactsFolder f = new ContactsFolder(this, services, cal, contactManager);
                    children.add(f);
                }
            }
        }
        return children;
    }

    @Override
    public Map<Principal, List<AccessControlledResource.Priviledge>> getAccessControlList() {
        return null;
    }

    @Override
    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException, NotFoundException {
        getServices().getTemplater().writePage("contactsHome.ftl", this, params, out, currentUser);
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

    public String getHref() {
        return parent.getHref() + name + "/";
    }
}

   
