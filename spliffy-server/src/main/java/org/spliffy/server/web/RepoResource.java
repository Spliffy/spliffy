package org.spliffy.server.web;

import com.bradmcevoy.http.*;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.exceptions.NotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.hashsplit4j.api.BlobStore;
import org.hashsplit4j.api.HashStore;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.spliffy.server.db.*;

/**
 * Represents a repository resource.
 *
 * @author brad
 */
public class RepoResource extends AbstractSpliffyResource implements CollectionResource, PropFindableResource, MakeCollectionableResource, GetableResource {

    private final Repository repository;
    
    private final VersionNumberGenerator versionNumberGenerator;
    
    private List<AbstractSpliffyResource> children;
    
    private long hash;
    
    public RepoResource(Repository repository, HashStore hashStore, BlobStore blobStore, VersionNumberGenerator versionNumberGenerator) {
        super(hashStore, blobStore);
        this.repository = repository;
        this.versionNumberGenerator = versionNumberGenerator;
        RepoVersion v = repository.latestVersion();
        if( v != null ) {
            hash = v.getDirHash();
        }
    }

    @Override
    public Resource child(String childName) {
        return Utils.childOf(getChildren(), childName);
    }

    @Override
    public List<AbstractSpliffyResource> getChildren() {
        if( children == null ) {
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
        System.out.println("RepoResource:  createCollection " + newName);
        Session session = MiltonOpenSessionInViewFilter.session();
        Transaction tx = session.beginTransaction();
        
        ResourceVersionMeta meta = Utils.newDirMeta();
        RepoDirectoryResource rdr = new RepoDirectoryResource(newName, meta, this, hashStore, blobStore);
        getChildren(); // ensure loaded
        children.add(rdr);
        
        onChildChanged(session);
        
        tx.commit();
                
        return rdr;
    }


    @Override
    public void onChildChanged(Session session) {
        System.out.println("onChildChanged: " + getName());
        getChildren();
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
    }

    @Override
    public Long getEntryHash() {
        throw new UnsupportedOperationException("Not supported yet.");
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
        if( type == null ) {
            // output directory listing
            DirectoryUtils.writeIndexPage(this, params);
        } else {
            if( type.equals("hashes")) {
                System.out.println("output hashes");
                HashCalc.calcResourceesHash(getChildren(), out);
                out.flush();
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
        if( type == null || type.length() == 0 ) {
            return "text/html";
        } else {
            if( type.equals("hashes")) {
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
    
}
