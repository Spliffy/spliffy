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
import com.bradmcevoy.http.values.ValueAndType;
import com.bradmcevoy.http.webdav.PropFindResponse;
import com.bradmcevoy.http.webdav.PropertySourcePatchSetter;
import com.bradmcevoy.property.BeanPropertyResource;
import com.ettrema.http.AddressResource;
import com.ettrema.http.acl.Principal;
import com.ettrema.ldap.LdapContact;
import java.io.*;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import org.apache.commons.io.IOUtils;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spliffy.server.apps.calendar.CalEventResource;
import org.spliffy.server.apps.calendar.CalendarFolder;
import org.spliffy.server.db.BaseEntity;
import org.spliffy.server.db.Contact;
import org.spliffy.server.db.utils.SessionManager;
import org.spliffy.server.db.User;
import org.spliffy.server.web.AbstractResource;
import org.spliffy.server.web.SpliffyCollectionResource;

/**
 *
 * @author brad
 */
@BeanPropertyResource(value="ldap")
public class ContactResource extends AbstractResource implements GetableResource, ReplaceableResource, AddressResource, LdapContact, DeletableResource, MoveableResource, CopyableResource, PropertySourcePatchSetter.CommitableResource {

    private static final Logger log = LoggerFactory.getLogger(CalEventResource.class);
    private final Contact contact;
    private final ContactsFolder parent;
    private final ContactManager contactManager;
    private Transaction tx; // for proppatch setting

    public ContactResource(ContactsFolder parent, Contact contact, ContactManager contactManager) {
        super(parent.getServices());
        this.contact = contact;
        this.parent = parent;
        this.contactManager = contactManager;
    }

    @Override
    public String getUniqueId() {
        return contact.getId().toString();
    }
        
    @Override
    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException {
        writeData(out);
        out.flush();
    }

    @Override
    public void replaceContent(InputStream in, Long length) throws BadRequestException, ConflictException, NotAuthorizedException {
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            IOUtils.copy(in, bout);
            String data = bout.toString();
            contactManager.update(contact, data);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void writeData(OutputStream out) {
        String s= contactManager.getContactAsCarddav(contact);
        PrintWriter pw = new PrintWriter(out);
        pw.print(s);
        pw.flush();

    }

    @Override
    public String getContentType(String accepts) {
        return "text/vcard";
    }

    @Override
    public String getName() {
        return contact.getName();
    }

    @Override
    public Date getModifiedDate() {
        return contact.getModifiedDate();
    }

    @Override
    public Date getCreateDate() {
        return contact.getCreatedDate();
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
    public void addPrivs(List<Priviledge> list, User user) {
        if (user != null && getOwner().containsUser(user)) {
            list.add(Priviledge.READ);
            list.add(Priviledge.WRITE);
            list.add(Priviledge.WRITE_ACL);
            list.add(Priviledge.WRITE_PROPERTIES);
        }
    }

    @Override
    public Map<Principal, List<Priviledge>> getAccessControlList() {
        return null;
    }

    @Override
    public void delete() throws NotAuthorizedException, ConflictException, BadRequestException {
        contactManager.delete(contact);
    }

    @Override
    public void moveTo(CollectionResource rDest, String name) throws ConflictException, NotAuthorizedException, BadRequestException {
        if (rDest instanceof ContactsFolder) {
            ContactsFolder calFolder = (ContactsFolder) rDest;
            contactManager.move(contact, calFolder.getAddressBook(), name);
        } else {
            throw new BadRequestException(rDest, "The destination resource is not a calendar");
        }
    }

    @Override
    public void copyTo(CollectionResource rDest, String name) throws NotAuthorizedException, BadRequestException, ConflictException {
        if (rDest instanceof CalendarFolder) {
            ContactsFolder calFolder = (ContactsFolder) rDest;
            contactManager.copy(contact, calFolder.getAddressBook(), name);
        } else {
            throw new BadRequestException(rDest, "The destination resource is not a calendar");
        }
    }

    @Override
    public void doCommit(Map<QName, ValueAndType> knownProps, Map<Response.Status, List<PropFindResponse.NameAndError>> errorProps) {
        if (tx == null) {
            log.warn("doCommit: Transaction not started");
        } else {
            log.trace("doCommit: commiting");
            SessionManager.session().save(this.contact);
            tx.commit();
        }
    }

    /**
     * Called from setters used by proppatch
     */
    private void checkTx() {
        if (tx == null) {
            tx = SessionManager.session().beginTransaction();
        }
    }

    @Override
    public Long getMaxAgeSeconds(Auth auth) {
        return null;
    }

    @Override
    public Long getContentLength() {
        return null;
    }

    @Override
    public String getAddressData() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writeData(out);
        return out.toString();
    }
    
    public String getFormattedName() {
        return contact.getGivenName() + " " + contact.getSurName();
    }
    
    public String getCommonName() {
        return getFormattedName();
    }
    
    public String getTelephone() {
        return contact.getTelephonenumber();
    }
    
    public String getEmail() {
        return contact.getMail();
    }
    
    public void setEmail(String s) {
        contact.setMail(s);
    }
    
    public String getOrganizationName() {
        return contact.getOrganizationName();
    }
    
    public void setOrganizationName(String s) {
        contact.setOrganizationName(s);
    }
    
    public String getTelephonenumber() {
        return contact.getTelephonenumber();
    }
    
    public void setTelephonenumber(String s) {
        contact.setTelephonenumber(s);
    }
    
    public String getMail() {
        return contact.getMail();
    }
    
    public void setMail(String mail) {
        contact.setMail(mail);
    }
    
    public String getGivenName() {
        return contact.getGivenName();
    }
    
    public void setGivenName(String s) {
        contact.setGivenName(s);
    }
    
    public String getSurName() {
        return contact.getSurName();
    }
    
    public void setSurName(String s) {
        contact.setSurName(s);
    }
}
