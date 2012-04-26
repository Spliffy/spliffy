package org.spliffy.server.web;

import com.bradmcevoy.http.*;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.exceptions.NotFoundException;
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
 * Represents a repository resource.
 *
 * This behaves much the same as a DirectoryResource but is defined
 * differently
 *
 * TODO: must support PUT
 *
 * @author brad
 */
public class RepositoryFolder extends AbstractCollectionResource implements MutableCollection, CollectionResource, PropFindableResource, MakeCollectionableResource, GetableResource, PutableResource {

    private final Repository repository;
    private final VersionNumberGenerator versionNumberGenerator;
    private List<MutableResource> children;
    private long hash;
    private boolean dirty;
    private RepoVersion repoVersion; // may be null
    private ItemVersion rootItemVersion;

    public RepositoryFolder(Repository repository, RepoVersion repoVersion, Services services, VersionNumberGenerator versionNumberGenerator) {
        super(services);
        this.repository = repository;
        this.versionNumberGenerator = versionNumberGenerator;
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
        System.out.println("createCollection: " + newName);
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
        System.out.println("RepoResource: save: dirty=" + isDirty());
        if (!isDirty()) {
            return;
        }

        try {
            System.out.println("calc child hashes..");
            for (MutableResource r : children) { // if is dirty then children must be loaded
                if (r instanceof MutableCollection) {
                    MutableCollection col = (MutableCollection) r;
                    calcHashes(session, col); // each collection checks its own dirty flag, won't do anything if clean
                }
            }

            ItemVersion newVersion = Utils.newItemVersion(getItemVersion(), getType());
            setItemVersion(newVersion);
            System.out.println("Inserted new root item version id: " + newVersion.getId() + " for: " + getName());

            saveCollection(session, this);

            RepoVersion newRepoVersion = new RepoVersion();
            newRepoVersion.setCreatedDate(new Date());
            newRepoVersion.setRepository(repository);
            newRepoVersion.setRootItemVersion(rootItemVersion);
            long newVersionNum = versionNumberGenerator.nextVersionNumber(repository);
            newRepoVersion.setVersionNum(newVersionNum);
            session.save(newRepoVersion);
            System.out.println("Saved new repo version: " + newVersionNum + " with hash: " + rootItemVersion.getItemHash());

        } catch (NotAuthorizedException | BadRequestException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void saveCollection(Session session, MutableCollection parent) throws NotAuthorizedException, BadRequestException {
        if (!parent.isDirty()) {
            return;
        }

        List<MutableResource> nextChildren = (List<MutableResource>) parent.getChildren();
        for (MutableResource r : nextChildren) { // if is dirty then children must be loaded
            insertMember(session, parent.getItemVersion(), r);
        }
    }

    /**
     * If the resource has itself changed, then create a new ItemVersion for
     * itself
     *
     * Create a new DirectoryMember to connect the ItemVersion of the member to
     * the new parent version.
     *
     *
     * @param session
     * @param parentItemVersion
     * @param r
     */
    private void insertMember(Session session, ItemVersion parentItemVersion, MutableResource r) throws NotAuthorizedException, BadRequestException {
        ItemVersion memberItemVersion;
        if (r.isDirty()) {
            memberItemVersion = Utils.newItemVersion(r.getItemVersion(), r.getType()); // create a new ItemVersion for the member
        } else {
            memberItemVersion = r.getItemVersion(); // use existing ItemVersion
        }
        memberItemVersion.setItemHash(r.getEntryHash());
        r.setItemVersion(memberItemVersion);
        DirectoryMember member = new DirectoryMember();
        member.setParentItem(parentItemVersion);
        member.setName(r.getName());
        member.setMemberItem(memberItemVersion);
        session.save(member);
        System.out.println("created member: " + member.getName() + "  on parent: " + parentItemVersion.getId() + " hash=" + memberItemVersion.getItemHash());

        if (r instanceof MutableCollection) {
            MutableCollection col = (MutableCollection) r;
            saveCollection(session, col); // will do dirty check
        }
    }

    /**
     * Check if this is dirty, and if so recalculate the hash for the directory
     *
     * A recursive call, recalculating children as necessary
     *
     * @param session
     */
    private void calcHashes(Session session, MutableCollection parent) throws NotAuthorizedException, BadRequestException {
        System.out.println("calcHashes: " + parent.getName() + " dirty=" + parent.isDirty());
        if (!parent.isDirty()) {
            return;
        }
        List<MutableResource> nextChildren = (List<MutableResource>) parent.getChildren();
        for (MutableResource r : nextChildren) { // if is dirty then children must be loaded
            if (r instanceof MutableCollection) {
                MutableCollection col = (MutableCollection) r;
                calcHashes(session, col);
            }
        }
        // calc new dirHash and save dir entries for children
        long newHash = HashCalc.calcResourceesHash(children);
        parent.setEntryHash(newHash);
        return;
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
        String type = HttpManager.request().getParams().get("type");
        if (type == null) {
            // output directory listing
            getTemplater().writePage("repoHome.ftl", this, params, out);
        } else {
            switch (type) {
                case "hashes":
                    HashCalc.calcResourceesHash(getChildren(), out);
                    out.flush();
                    break;
                case "revision":
                    // write the version number (revision) followed by the directory hash
                    RepoVersion rv = repository.latestVersion();
                    try (DataOutputStream dout = new DataOutputStream(out)) {
                        dout.writeLong(rv.getVersionNum());
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

    /**
     * may be null
     *
     * @return
     */
    public RepoVersion getRepoVersion() {
        return repoVersion;
    }

    @Override
    public MutableCollection getParent() {
        return null; // no mutable parents
    }

    @Override
    public String getType() {
        return "d";
    }
}
