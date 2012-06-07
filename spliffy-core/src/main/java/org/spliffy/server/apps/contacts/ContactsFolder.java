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

import com.bradmcevoy.common.InternationalizedString;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.DeletableResource;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.PutableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.ReportableResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.exceptions.NotFoundException;
import com.bradmcevoy.http.values.Pair;
import com.ettrema.http.AddressBookResource;
import com.ettrema.http.acl.Principal;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.spliffy.server.db.*;
import org.spliffy.server.web.*;

/**
 * Represents an Address Book
 *
 * @author brad
 */
public class ContactsFolder extends AbstractCollectionResource implements AddressBookResource, ReportableResource, GetableResource, DeletableResource, PutableResource {
    private final ContactsHomeFolder parent;
    private final AddressBook addressBook;
    private final ContactManager contactManager;
    
    private List<ContactResource> children;

    public ContactsFolder(ContactsHomeFolder parent, Services services, AddressBook addressBook, ContactManager contactManager) {
        super(services);
        this.parent = parent;
        this.addressBook = addressBook;
        this.contactManager = contactManager;
    }

    public String getHref() {
        return parent.getHref() + this.getName() + "/";
    }
    
    @Override
    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException, NotFoundException {
        services.getHtmlTemplater().writePage("contacts", this, params, out);
    }
    
    
    @Override
    public void delete() throws NotAuthorizedException, ConflictException, BadRequestException {
        contactManager.delete(addressBook);
    }    
    
    @Override
    public SpliffyCollectionResource getParent() {
        return parent;
    }

    @Override
    public BaseEntity getOwner() {
        return getParent().getOwner();
    }

    @Override
    public void addPrivs(List<Priviledge> list, Profile user) {
        getParent().addPrivs(list, user);
    }

    @Override
    public String getName() {
        return addressBook.getName();
    }

    @Override
    public Date getModifiedDate() {
        return addressBook.getModifiedDate();
    }

    @Override
    public Date getCreateDate() {
        return addressBook.getCreatedDate();
    }

    @Override
    public Resource child(String childName) throws NotAuthorizedException, BadRequestException {
        return Utils.childOf(getChildren(), childName);
    }

    @Override
    public List<? extends Resource> getChildren() throws NotAuthorizedException, BadRequestException {
        if( children == null ) {
            children = new ArrayList<>();
            if( addressBook.getContacts() != null ) {
                for( Contact c : addressBook.getContacts() ) {
                    ContactResource r = new ContactResource(this, c, contactManager);
                    children.add(r);
                }
            }
        }
        return children;
    }


    @Override
    public String getCTag() {
        return addressBook.getModifiedDate().getTime() + "t";
    }
    
    @Override
    public Map<Principal, List<Priviledge>> getAccessControlList() {
        return null;
    }

    @Override
    public Resource createNew(String newName, InputStream inputStream, Long length, String contentType) throws IOException, ConflictException, NotAuthorizedException, BadRequestException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        IOUtils.copy(inputStream, bout);
        String data = bout.toString();
        Contact contact= contactManager.createContact(addressBook, newName, data, contentType);
        return new ContactResource(this, contact, contactManager);
    }

    public AddressBook getAddressBook() {
        return addressBook;
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
    public InternationalizedString getDescription() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setDescription(InternationalizedString description) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Pair<String, String>> getSupportedAddressData() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Long getMaxResourceSize() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public Organisation getOrganisation() {
        return parent.getOrganisation();
    }
 
}