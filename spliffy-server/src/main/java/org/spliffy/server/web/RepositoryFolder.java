package org.spliffy.server.web;

import org.spliffy.server.db.utils.SessionManager;
import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.*;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.exceptions.NotFoundException;
import com.ettrema.http.AccessControlledResource;
import com.ettrema.http.acl.Principal;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.hashsplit4j.api.Parser;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.spliffy.server.db.*;

/**
 * Represents the current version of the trunk of a repository
 *
 * This behaves much the same as a DirectoryResource but is defined
 * differently
 *
 * TODO: must support PUT
 *
 * @author brad
 */
public class RepositoryFolder extends AbstractCollectionResource implements MutableCollection, CollectionResource, PropFindableResource, MakeCollectionableResource, GetableResource, PutableResource, PostableResource {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RepositoryFolder.class);
    
    private final Repository repository;
    
    private final SpliffyCollectionResource parent;
    private List<MutableResource> children;
    private long hash;
    private boolean dirty;
    private Commit repoVersion; // may be null
    private ItemVersion rootItemVersion;
    
    private JsonResult jsonResult; // set after completing a POST

    public RepositoryFolder(SpliffyCollectionResource parent, Repository repository, Commit repoVersion) {
        super(parent.getServices());
        this.parent = parent;
        this.repository = repository;
        this.repoVersion = repoVersion;
        if (repoVersion != null) {
            rootItemVersion = repoVersion.getRootItemVersion();
            if (rootItemVersion != null) {
                hash = rootItemVersion.getItemHash();
            }
        }
    }
    
    

    @Override
    public Resource child(String childName) {
        return Utils.childOf(getChildren(), childName);
    }

    @Override
    public List<MutableResource> getChildren() {
        if (children == null) {
            if (repoVersion != null) {
                List<DirectoryMember> members = repoVersion.getRootItemVersion().getMembers();
                children = Utils.toResources(this, members);
            } else {
                children = new ArrayList<>();
            }
        }
        return children;
    }

    @Override
    public String getName() {
        return repository.getName();
    }

    @Override
    public CollectionResource createCollection(String newName) throws NotAuthorizedException, ConflictException, BadRequestException {
        log.trace("createCollection: " + newName);
        Session session = SessionManager.session();
        Transaction tx = session.beginTransaction();

        ItemVersion newItemVersion = Utils.newDirItemVersion();
        DirectoryResource rdr = new DirectoryResource(newName, newItemVersion, this, services);
        addChild(rdr);
        save(session);

        tx.commit();

        return rdr;
    }

    @Override
    public Resource createNew(String newName, InputStream inputStream, Long length, String contentType) throws IOException, ConflictException, NotAuthorizedException, BadRequestException {
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

        } else {
            // parse data and persist to stores
            Parser parser = new Parser();
            long fileHash = parser.parse(inputStream, getHashStore(), getBlobStore());

            // add a reference to the new child
            getChildren();
            fileResource.setHash(fileHash);
        }
        addChild(fileResource);

        save(session);
        SessionManager.session().save(newMeta);

        tx.commit();

        return fileResource;

    }

    /**
     * Save procedure is:
     *
     * 1. as resources are changed they set their dirty flag, which propogates
     * up tp the parent. If in save the dirty flag on RepositoryFolder is false,
     * then nothing has changed - exit<br/>
     *
     * 2.Recalculate hashes on all dirty directories<br/>
     *
     * 3. If dirty, insert a new root item version
     *
     * 4. for each member
     *
     * 4a. if dirty create a new item version,
     *
     * 4b. insert member record connecting it to this version
     *
     * 4c. go to step 3
     *
     * @param session
     * @return
     */
    @Override
    public void save(Session session) {
        log.trace("save");
        services.getResourceManager().save(session, this);
    }

    @Override
    public Long getEntryHash() {
        return hash;
    }

    @Override
    public void setEntryHash(long newHash) {
        this.hash = newHash;
    }

    @Override
    public ItemVersion getItemVersion() {
        return rootItemVersion;
    }

    @Override
    public void setItemVersion(ItemVersion newVersion) {
        log.trace("setItemVersion");
        //this.dirty = false;
        this.rootItemVersion = newVersion;
    }
        

    @Override
    public Date getCreateDate() {
        return repository.getCreatedDate();
    }

    @Override
    public Date getModifiedDate() {
        if (rootItemVersion != null) {
            return rootItemVersion.getModifiedDate();
        }
        return null;
    }

    @Override
    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException, NotFoundException {
        if (jsonResult != null) {
            jsonResult.write(out);
            return;
        }               
        String type = params.get("type");
        if (type == null) {
            // output directory listing
            log.trace("sendContent: render template");
            getTemplater().writePage("repoHome.ftl", this, params, out, getCurrentUser());
        } else {
            log.trace("sendContent: " + type);
            switch (type) {
                case "hashes":
                    HashCalc.calcResourceesHash(getChildren(), out);
                    out.flush();
                    break;
                case "revision":
                    // write the directory hash
                    Commit rv = repository.latestVersion();
                    try (DataOutputStream dout = new DataOutputStream(out)) {
                        dout.writeLong(rv.getRootItemVersion().getItemHash());
                    }
                    break;
            }
        }
    }

    @Override
    public Long getMaxAgeSeconds(Auth auth) {
        return null;
    }

    @Override
    public String getContentType(String accepts) {
        if( jsonResult != null ) {
            return "application/x-javascript; charset=utf-8";
        }        
        String type = HttpManager.request().getParams().get("type");
        if (type == null || type.length() == 0) {
            return "text/html";
        } else {
            if (type.equals("hashes") || type.equals("revision")) {
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
    public void removeChild(MutableResource r) {
        dirty = true;
        getChildren().remove(r);
    }

    @Override
    public void addChild(MutableResource r) throws NotAuthorizedException, BadRequestException {
        dirty = true;
        getChildren().add(r);
    }

    @Override
    public void onChildChanged(MutableResource r) {
        dirty = true;
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }
    
    

    /**
     * may be null
     *
     * @return
     */
    public Commit getRepoVersion() {
        return repoVersion;
    }

    @Override
    public SpliffyCollectionResource getParent() {
        return parent;
    }

    @Override
    public String getType() {
        return "d";
    }

    @Override
    public BaseEntity getOwner() {
        return parent.getOwner();
    }
    
    @Override
    public void addPrivs(List<Priviledge> list, User user) {
        parent.addPrivs(list, user);
    }

    /**
     * Get all allowed priviledges for all principals on this resource. Note
     * that a principal might be a user, a group, or a built-in webdav group
     * such as AUTHENTICATED
     *
     * @return
     */
    @Override
    public Map<Principal, List<AccessControlledResource.Priviledge>> getAccessControlList() {
        ItemVersion v = this.getItemVersion();
        if (v == null) {
            return null;
        } else {
            List<Permission> perms = v.getItem().getGrantedPermissions();
            Map<Principal, List<AccessControlledResource.Priviledge>> map = SecurityUtils.toMap(perms);
            return map;
        }
    }

    /**
     * Return the direct (not linked) repository. That is, the direct parent
     * of the RepositoryVersion object this resource is listing children for
     * @return 
     */
    public Branch getDirectRepository() {
        if( this.repoVersion != null ) {
            return repoVersion.getBranch();
        }
        return repository.trunk(SessionManager.session());
    }

    public ItemVersion getRootItemVersion() {
        return rootItemVersion;
    }

    @Override
    public DirectoryMember getDirectoryMember() {
        return null;
    }

    @Override
    public Path getPath() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String processForm(Map<String, String> parameters, Map<String, FileItem> files) throws BadRequestException, NotAuthorizedException, ConflictException {
        String shareWith = parameters.get("shareWith");
        String priv = parameters.get("priviledge");
        AccessControlledResource.Priviledge p = AccessControlledResource.Priviledge.valueOf(priv);
        String message = parameters.get("message");
        if (shareWith != null) {
            getServices().getShareManager().sendShareInvites(currentUser, repository, shareWith, p, message);
            this.jsonResult = new JsonResult(true);
        }
        return null;
    
    }
    
    
}
