package org.spliffy.server.web;

import com.bradmcevoy.http.*;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.exceptions.NotFoundException;
import java.io.*;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.hashsplit4j.api.BlobStore;
import org.hashsplit4j.api.HashStore;
import org.hashsplit4j.api.Parser;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.spliffy.server.db.*;

/**
 * Represents a repository resource.
 *
 * This behaves much the same as a RepoDirectoryResource but is defined
 * differently
 *
 * TODO: must support PUT
 *
 * @author brad
 */
public class RepoResource extends AbstractSpliffyCollectionResource implements MutableCollection, CollectionResource, PropFindableResource, MakeCollectionableResource, GetableResource, PutableResource {

    private final Repository repository;
    private final VersionNumberGenerator versionNumberGenerator;
    private List<MutableResource> children;
    private long hash;
    private boolean dirty;

    public RepoResource(Repository repository, RepoVersion repoVersion, HashStore hashStore, BlobStore blobStore, VersionNumberGenerator versionNumberGenerator) {
        super(hashStore, blobStore);
        this.repository = repository;
        this.versionNumberGenerator = versionNumberGenerator;
        if (repoVersion != null) {
            hash = repoVersion.getDirHash();
        }
    }

    @Override
    public Resource child(String childName) {
        if (childName.equals("revisions")) {
            return new RepoRevisionsResource(repository);
        }

        return Utils.childOf(getChildren(), childName);
    }

    @Override
    public List<MutableResource> getChildren() {
        if (children == null) {
            List<DirEntry> dirEntries = DirEntry.listEntries(MiltonOpenSessionInViewFilter.session(), hash);
            children = Utils.toResources(this, dirEntries);
        }
        return children;
    }

    @Override
    public String getName() {
        return repository.getName();
    }

    @Override
    public CollectionResource createCollection(String newName) throws NotAuthorizedException, ConflictException, BadRequestException {
        Session session = MiltonOpenSessionInViewFilter.session();
        Transaction tx = session.beginTransaction();

        ResourceVersionMeta meta = Utils.newDirMeta();
        RepoDirectoryResource rdr = new RepoDirectoryResource(newName, meta, this, hashStore, blobStore);
        addChild(rdr);        
        save(session);

        tx.commit();

        return rdr;
    }

    @Override
    public Resource createNew(String newName, InputStream inputStream, Long length, String contentType) throws IOException, ConflictException, NotAuthorizedException, BadRequestException {
        Session session = MiltonOpenSessionInViewFilter.session();
        Transaction tx = session.beginTransaction();

        ResourceVersionMeta newMeta = Utils.newFileMeta();
        RepoFileResource fileResource = new RepoFileResource(newName, newMeta, this, getHashStore(), getBlobStore());

        String ct = HttpManager.request().getContentTypeHeader();
        if (ct != null && ct.equals("spliffy/hash")) {
            // read the new hash and set it on this
            DataInputStream din = new DataInputStream(inputStream);

            long newHash = din.readLong();
            fileResource.setHash(newHash);

        } else {
            // parse data and persist to stores
            Parser parser = new Parser();
            long fileHash = parser.parse(inputStream, hashStore, blobStore);

            // add a reference to the new child
            getChildren();
            fileResource.setHash(fileHash);
        }
        children.add(fileResource);

        long repoVersionNum = save(session);
        newMeta.setRepoVersionNum(repoVersionNum);
        MiltonOpenSessionInViewFilter.session().save(newMeta);

        tx.commit();

        return fileResource;

    }

    @Override
    public long save(Session session) {
        if( !isDirty() ) {
            return hash;
        }
        for( MutableResource r : children ) { // if is dirty then children must be loaded
            if( r instanceof RepoDirectoryResource ) {
                RepoDirectoryResource col = (RepoDirectoryResource) r;
                col.saveHashes(session); // each collection checks its own dirty flag, won't do anything if clean
            }
        }
        long repVersionHash = HashCalc.calcResourceesHash(children);

        HashCalc.saveKids(session, repVersionHash, children);

        // need to create a new RepoVersion hash
        RepoVersion rv = new RepoVersion();
        rv.setId(UUID.randomUUID());
        long versionNum = versionNumberGenerator.nextVersionNumber(repository);
        rv.setVersionNum(versionNum);
        rv.setCreatedDate(new Date());
        rv.setDirHash(repVersionHash);
        rv.setRepository(repository);
        
        session.save(rv);

        return versionNum;
    }

    @Override
    public Long getEntryHash() {
        return hash;
    }

    @Override
    public UUID getMetaId() {
        return null;
    }

    @Override
    public Date getCreateDate() {
        return repository.getCreatedDate();
    }

    @Override
    public Date getModifiedDate() {
        return null;
    }

    @Override
    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException, NotFoundException {
        String type = HttpManager.request().getParams().get("type");
        if (type == null) {
            // output directory listing
            DirectoryUtils.writeIndexPage(this, params);
        } else {
            if (type.equals("hashes")) {
                HashCalc.calcResourceesHash(getChildren(), out);
                out.flush();
            } else if (type.equals("revision")) {
                // write the version number (revision) followed by the directory hash
                RepoVersion rv = repository.latestVersion();
                try (DataOutputStream dout = new DataOutputStream(out)) {
                    dout.writeLong(rv.getVersionNum());
                    dout.writeLong(rv.getDirHash());
                }
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
        System.out.println("onChildChanged: " + getName());
        dirty = true;
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }
}
