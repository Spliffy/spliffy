package org.spliffy.server.web;

import com.bradmcevoy.http.*;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.exceptions.NotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.hashsplit4j.api.BlobStore;
import org.hashsplit4j.api.HashStore;
import org.hashsplit4j.api.Parser;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.spliffy.server.db.DirEntry;
import org.spliffy.server.db.MiltonOpenSessionInViewFilter;
import org.spliffy.server.db.ResourceVersionMeta;

/**
 * Represents a version of a directory, containing the members which 
 * are in that directory in the repository snapshot
 *
 * @author brad
 */
public class RepoDirectoryResource extends AbstractSpliffyResource implements PropFindableResource, CollectionResource, MakeCollectionableResource, PutableResource, GetableResource {
    
    private AbstractSpliffyResource parent;
    
    private String name;
    
    private long dirHash; // hash of members of this directory
    
    private final ResourceVersionMeta meta;
    
    private List<AbstractSpliffyResource> children;
        
    public RepoDirectoryResource(String name, ResourceVersionMeta meta, AbstractSpliffyResource parent, HashStore hashStore, BlobStore blobStore) {
        super(hashStore, blobStore);
        this.meta = meta;
        this.name = name;
        this.parent = parent;
        if( parent == null ) {
            throw new IllegalArgumentException("parent is null");
        }
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getDirHash() {
        return dirHash;
    }

    public void setDirHash(long dirHash) {
        this.dirHash = dirHash;
    }
    
            
    @Override
    public Resource child(String childName) throws NotAuthorizedException, BadRequestException {
        return Utils.childOf(getChildren(), childName);
    }

    @Override
    public List<AbstractSpliffyResource> getChildren() throws NotAuthorizedException, BadRequestException {
        if( children == null ) {
            List<DirEntry> childDirEntries = DirEntry.listEntries(MiltonOpenSessionInViewFilter.session(), dirHash);
            children = Utils.toResources(this, childDirEntries);
        }
        return children;
    }

    @Override
    public CollectionResource createCollection(String newName) throws NotAuthorizedException, ConflictException, BadRequestException {
        Session session = MiltonOpenSessionInViewFilter.session();
        Transaction tx = session.beginTransaction();
        
        ResourceVersionMeta newMeta = Utils.newDirMeta();
        RepoDirectoryResource rdr = new RepoDirectoryResource(newName, newMeta, this, hashStore, blobStore);
        getChildren(); // ensure loaded
        children.add(rdr);
        
        onChildChanged(session);
        
        tx.commit();
                
        return rdr;
    }

    @Override
    public void onChildChanged(Session session) {
        System.out.println("onChildChanged: " + getName());
        
        // calc new dirHash and save dir entries for children
        dirHash = HashCalc.calcResourceesHash(children);
        HashCalc.saveKids(session, dirHash, children);
        
        // update parent
        parent.onChildChanged(session);
    }

    @Override
    public Long getEntryHash() {
        return dirHash;
    }

    @Override
    public UUID getMetaId() {
        return meta.getId();
    }

    @Override
    public Resource createNew(String newName, InputStream inputStream, Long length, String contentType) throws IOException, ConflictException, NotAuthorizedException, BadRequestException {
        Session session = MiltonOpenSessionInViewFilter.session();
        Transaction tx = session.beginTransaction();
        
        // parse data and persist to stores
        Parser parser = new Parser();
        long fileHash = parser.parse(inputStream, hashStore, blobStore);        
        
        // add a reference to the new child
        getChildren();
        ResourceVersionMeta newMeta = Utils.newFileMeta();
        RepoFileResource fileResource = new RepoFileResource(newName, newMeta, this, getHashStore(), getBlobStore());
        fileResource.setHash(fileHash);
        children.add(fileResource);
        
        onChildChanged(session);
                
        tx.commit();
        
        return fileResource;
    }

    @Override
    public Date getCreateDate() {
        return meta.getResourceMeta().getCreateDate();
    }

    @Override
    public Date getModifiedDate() {
        return meta.getModifiedDate();
    }

    @Override
    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException, NotFoundException {
        String type = HttpManager.request().getParams().get("type");
        if( type == null ) {
            // output directory listing
            DirectoryUtils.writeIndexPage(this, params);
        } else {
            if( type.equals("hashes")) {
                HashCalc.calcResourceesHash(getChildren(), out);
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
