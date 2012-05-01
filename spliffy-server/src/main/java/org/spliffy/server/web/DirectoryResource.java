package org.spliffy.server.web;

import com.bradmcevoy.http.*;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.exceptions.NotFoundException;
import com.ettrema.mail.MailboxAddress;
import com.ettrema.mail.StandardMessage;
import com.ettrema.mail.StandardMessageImpl;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import org.hashsplit4j.api.Parser;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.spliffy.server.db.*;

/**
 * Represents a version of a directory, containing the members which are in that
 * directory in the repository snapshot
 *
 * @author brad
 */
public class DirectoryResource extends AbstractMutableResource implements PutableResource, GetableResource, MutableCollection, PostableResource {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DirectoryResource.class);
    private List<MutableResource> children;
    private JsonResult jsonResult; // set after completing a POST
    private boolean dirty;

    public DirectoryResource(String name, ItemVersion meta, MutableCollection parent, Services services) {
        super(name, meta, parent, services);
    }

    @Override
    public void copyTo(CollectionResource toCollection, String newName) throws NotAuthorizedException, BadRequestException, ConflictException {
        if (toCollection instanceof MutableCollection) {
            Session session = SessionManager.session();
            Transaction tx = session.beginTransaction();

            MutableCollection newParent = (MutableCollection) toCollection;
            ItemVersion newMeta = Utils.newFileItemVersion();
            DirectoryResource newDir = new DirectoryResource(newName, newMeta, newParent, services);
            newDir.setHash(hash);
            newParent.addChild(newDir);
            newParent.save(session);
            tx.commit();
        } else {
            throw new ConflictException(this, "Can't copy to collection of type: " + toCollection.getClass());
        }
    }

    @Override
    public void removeChild(MutableResource r) throws NotAuthorizedException, BadRequestException {
        setDirty(true);
        getChildren().remove(r);
        parent.onChildChanged(this);
    }

    @Override
    public void addChild(MutableResource r) throws NotAuthorizedException, BadRequestException {
        log.trace("addChild: " + getName());
        Resource existing = child(r.getName());
        if (existing != null) {
            MutableResource mr = (MutableResource) existing;
            removeChild(mr);
        }
        setDirty(true);
        getChildren().add(r);
        parent.onChildChanged(this);
    }

    @Override
    public void onChildChanged(MutableResource r) {
        setDirty(true);
        parent.onChildChanged(this);
    }

    @Override
    public Resource child(String childName) throws NotAuthorizedException, BadRequestException {
        return Utils.childOf(getChildren(), childName);
    }

    @Override
    public List<MutableResource> getChildren() throws NotAuthorizedException, BadRequestException {
        if (children == null) {
            if (getItemVersion() != null) {
                List<DirectoryMember> members = getItemVersion().getMembers();
                children = Utils.toResources(this, members);
            } else {
                children = new ArrayList<>();
            }
        }
        return children;
    }

    @Override
    public CollectionResource createCollection(String newName) throws NotAuthorizedException, ConflictException, BadRequestException {
        Session session = SessionManager.session();
        Transaction tx = session.beginTransaction();

        ItemVersion newMeta = Utils.newDirItemVersion();
        DirectoryResource rdr = new DirectoryResource(newName, newMeta, this,  services);
        addChild(rdr);
        save(session);

        tx.commit();

        return rdr;
    }

    /**
     * Just call up to the RepoResource. It will call back to generate and save
     * hashes for all directories, then it will save a new RepoVersion
     *
     * @param session
     * @return
     */
    @Override
    public void save(Session session) {
        parent.save(session);
    }

    @Override
    public Resource createNew(String newName, InputStream inputStream, Long length, String contentType) throws IOException, ConflictException, NotAuthorizedException, BadRequestException {
        System.out.println("CreateNew: " + newName);
        Session session = SessionManager.session();
        Transaction tx = session.beginTransaction();

        ItemVersion newMeta = Utils.newFileItemVersion();
        FileResource fileResource = new FileResource(newName, newMeta, this, services);

        String ct = HttpManager.request().getContentTypeHeader();
        if (ct != null && ct.equals("spliffy/hash")) {
            // read the new hash and set it on this
            DataInputStream din = new DataInputStream(inputStream);

            long newHash = din.readLong();
            fileResource.setHash(newHash);
            System.out.println("Set hash(a): " + newHash + " on resource: " + fileResource.getName());
        } else {
            // parse data and persist to stores
            Parser parser = new Parser();
            long fileHash = parser.parse(inputStream, getHashStore(), getBlobStore());

            fileResource.setHash(fileHash);
            System.out.println("Set hash(b): " + fileHash + " on resource: " + fileResource.getName());
        }
        addChild(fileResource);
        save(session);
        tx.commit();

        return fileResource;
    }

    @Override
    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException, NotFoundException {
        if (jsonResult != null) {
            jsonResult.write(out);
            return;
        }
        String type = HttpManager.request().getParams().get("type");
        if (type == null) {
            // output directory listing
            getTemplater().writePage("directoryIndex.ftl", this, params, out);
        } else {
            if (type.equals("hashes")) {
                HashCalc.calcResourceesHash(getChildren(), out);
            }
        }
    }

    @Override
    public String getContentType(String accepts) {
        String type = HttpManager.request().getParams().get("type");
        if (type == null || type.length() == 0) {
            return "text/html";
        } else {
            if (type.equals("hashes")) {
                return "text/plain";
            } else {
                return type;
            }
        }
    }

    @Override
    public Long getContentLength() {
        return null;
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public void setDirty(boolean dirty) {
        log.trace("setDirty: " + dirty + "  on : " + getName());
        this.dirty = dirty;
    }

    @Override
    public boolean isDir() {
        return true;
    }

    @Override
    public String getType() {
        return "d";
    }

    @Override
    public void setEntryHash(long hash) {
        this.hash = hash;
    }

    /**
     * Supports sending folder share invitations by email. Note that this is
     * intended to be used with ajax, so returns a JSON result
     *
     * It does this by settnig the generic JsonResult object on the resource
     *
     * @param parameters
     * @param files
     * @return
     * @throws BadRequestException
     * @throws NotAuthorizedException
     * @throws ConflictException
     */
    @Override
    public String processForm(Map<String, String> parameters, Map<String, FileItem> files) throws BadRequestException, NotAuthorizedException, ConflictException {
        String shareWith = parameters.get("shareWith");
        String priv = parameters.get("priviledge");
        String message = parameters.get("message");
        if (shareWith != null) {
            Session session = SessionManager.session();
            Transaction tx = session.beginTransaction();
            List<StandardMessage> toSend = createShares(shareWith, priv, message, session);
            tx.commit();
            sendMail(toSend);
            this.jsonResult = new JsonResult(true);
        }
        return null;
    }

    private List<StandardMessage> createShares(String shareWith, String priv, String message, Session session) {
        Priviledge p = Priviledge.valueOf(priv);
        String[] arr = shareWith.split(",");
        User curUser = getCurrentUser();
        if (curUser == null) {
            throw new RuntimeException("No current user");
        }
        MailboxAddress from = MailboxAddress.parse(curUser.getEmail());
        List<StandardMessage> list = new ArrayList<>();
        String inviteBaseHref = HttpManager.request().getAbsoluteUrl(); // http://asdada/asd/ad
        inviteBaseHref = inviteBaseHref.substring(0, inviteBaseHref.indexOf("/", 7) + 1);// http://asdada/
        inviteBaseHref += "shares/";
        System.out.println("invite base href: " + inviteBaseHref);
        for (String recip : arr) {
            System.out.println("Send invite to: " + recip);
            StandardMessageImpl sm = createEmailShare(from, recip, p, message, inviteBaseHref, session);
            list.add(sm);
        }
        return list;
    }

    private void sendMail(List<StandardMessage> list) {
        for (StandardMessage sm : list) {
            getServices().getMailSender().sendMail(sm);
        }

    }

    private StandardMessageImpl createEmailShare(MailboxAddress from, String sRecip, Priviledge p, String message, String inviteBaseHref, Session session) {

        Link link = new Link();
        link.setId(UUID.randomUUID());
        link.setSharedFrom(getItemVersion().getItem());
        link.setShareRecip(sRecip);
        link.setPriviledge(p);
        session.save(link);

        String inviteHref = inviteBaseHref + link.getId();

        MailboxAddress recip = MailboxAddress.parse(sRecip);

        StandardMessageImpl sm = new StandardMessageImpl();
        sm.setFrom(from);
        sm.setTo(Arrays.asList(recip));
        sm.setSubject("Share folder");
        System.out.println("Share invitation href: " + inviteHref);
        sm.setText(message + "\n\nTo see the files please click here: " + inviteHref);
        getServices().getMailSender().sendMail(sm);
        return sm;
    }
}
