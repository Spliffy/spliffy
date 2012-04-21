package org.spliffy.server.web;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import java.util.Date;
import java.util.UUID;
import org.hashsplit4j.api.BlobStore;
import org.hashsplit4j.api.HashStore;
import org.hibernate.Session;

/**
 *
 * @author brad
 */
public abstract class AbstractSpliffyResource implements PropFindableResource {

    public abstract void onChildChanged(Session session);
    
    public abstract Long getEntryHash();
    
    public abstract UUID getMetaId();

    protected final HashStore hashStore;
    
    protected final BlobStore blobStore;

    public AbstractSpliffyResource(HashStore hashStore, BlobStore blobStore) {
        this.hashStore = hashStore;
        this.blobStore = blobStore;
    }

    @Override
    public String getUniqueId() {
        return null;
    }

    @Override
    public Object authenticate(String user, String password) {
        return user;
    }

    @Override
    public boolean authorise(Request request, Method method, Auth auth) {
        return true;
    }

    @Override
    public String getRealm() {
        return "spliffy";
    }

    @Override
    public String checkRedirect(Request request) {
        return null;
    }

    public BlobStore getBlobStore() {
        return blobStore;
    }

    public HashStore getHashStore() {
        return hashStore;
    }

    
}
