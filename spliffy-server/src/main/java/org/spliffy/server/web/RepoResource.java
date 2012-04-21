package org.spliffy.server.web;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.MakeCollectionableResource;
import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.hashsplit4j.api.BlobStore;
import org.hashsplit4j.api.HashStore;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.spliffy.server.db.*;

/**
 *
 * @author brad
 */
public class RepoResource extends AbstractSpliffyResource implements CollectionResource, PropFindableResource, MakeCollectionableResource {

    private final Repository repository;

    private List<AbstractSpliffyResource> children;
    
    private long hash;
    
    public RepoResource(Repository repository, HashStore hashStore, BlobStore blobStore) {
        super(hashStore, blobStore);
        this.repository = repository;
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
    public List<? extends Resource> getChildren() {
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
        
        ResourceMeta meta = Utils.newDirMeta();
        RepoDirectoryResource rdr = new RepoDirectoryResource(newName, meta, this, hashStore, blobStore);
        getChildren(); // ensure loaded
        children.add(rdr);
        
        onChildChanged(session);
        
        tx.commit();
                
        return rdr;
    }


    @Override
    public void onChildChanged(Session session) {
        getChildren();
        long repVersionHash = HashCalc.calcResourceesHash(children);
        
        HashCalc.saveKids(session, repVersionHash, children);
        
        // need to create a new RepoVersion hash
        RepoVersion rv = new RepoVersion();
        rv.setId(UUID.randomUUID());
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
    
}
