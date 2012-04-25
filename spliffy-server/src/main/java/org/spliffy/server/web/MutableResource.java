package org.spliffy.server.web;

import com.bradmcevoy.http.Resource;
import java.util.UUID;
import org.hashsplit4j.api.BlobStore;
import org.hashsplit4j.api.HashStore;

/**
 *
 * @author brad
 */
public interface MutableResource extends Resource {

    BlobStore getBlobStore();

    HashStore getHashStore();
    
    Services getServices();
    
    public abstract Long getEntryHash();
    
    public abstract UUID getMetaId();    
}
