package org.spliffy.server.web;

import com.bradmcevoy.http.*;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.exceptions.NotFoundException;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.hashsplit4j.api.Parser;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.spliffy.server.db.DirectoryMember;
import org.spliffy.server.db.ItemVersion;
import org.spliffy.server.db.SessionManager;

/**
 * Represents a version of a directory, containing the members which are in that
 * directory in the repository snapshot
 *
 * @author brad
 */
public class RepoDirectoryResource extends AbstractMutableSpliffyResource implements PutableResource, GetableResource, MutableCollection {

    private List<MutableResource> children;

    public RepoDirectoryResource(String name, ItemVersion meta, MutableCollection parent, Services services) {
        super(name, meta, parent, services);
    }

    @Override
    public void copyTo(CollectionResource toCollection, String newName) throws NotAuthorizedException, BadRequestException, ConflictException {
        if (toCollection instanceof MutableCollection) {
            Session session = SessionManager.session();
            Transaction tx = session.beginTransaction();

            MutableCollection newParent = (MutableCollection) toCollection;
            ItemVersion newMeta = Utils.newFileItemVersion();
            RepoDirectoryResource newDir = new RepoDirectoryResource(newName, newMeta, newParent, services);
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
        dirty = true;
        getChildren().remove(r);
        parent.onChildChanged(this);
    }

    @Override
    public void addChild(MutableResource r) throws NotAuthorizedException, BadRequestException {
        Resource existing = child(r.getName());
        if (existing != null) {
            MutableResource mr = (MutableResource) existing;
            removeChild(mr);
        }
        dirty = true;
        getChildren().add(r);
        parent.onChildChanged(this);
    }

    @Override
    public void onChildChanged(MutableResource r) {
        dirty = true;
        parent.onChildChanged(this);
    }

    @Override
    public Resource child(String childName) throws NotAuthorizedException, BadRequestException {
        return Utils.childOf(getChildren(), childName);
    }

    @Override
    public List<MutableResource> getChildren() throws NotAuthorizedException, BadRequestException {
        if (children == null) {
            if( getItemVersion() != null ) {
                System.out.println("get members of version: " + getItemVersion().getId());
                List<DirectoryMember> members = getItemVersion().getMembers();
                System.out.println("members: " + members.size());
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
        RepoDirectoryResource rdr = new RepoDirectoryResource(newName, newMeta, this, services);
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
        RepoFileResource fileResource = new RepoFileResource(newName, newMeta, this, services);

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
    public MutableCollection getParent() {
        return parent;
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
}
