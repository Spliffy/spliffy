package org.spliffy.server.web;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import java.util.UUID;
import org.hashsplit4j.api.BlobStore;
import org.hashsplit4j.api.HashStore;
import org.spliffy.server.db.ItemVersion;

/**
 *
 * @author brad
 */
public abstract class AbstractSpliffyResource implements PropFindableResource {


    
    public abstract Long getEntryHash();
    
    public abstract ItemVersion getItemVersion();
    
    /**
     * For templating, return true if this is a directory, false for a file
     */
    public abstract boolean isDir();

    protected final Services services;

    public AbstractSpliffyResource(Services services) {
        this.services = services;
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
        return services.getBlobStore();
    }

    public HashStore getHashStore() {
        return services.getHashStore();
    }
    
    public Templater getTemplater() {
        return services.getTemplater();
    }

    public Services getServices() {
        return services;
    }

    
    
}
