package org.spliffy.server.web;

import org.hashsplit4j.api.BlobStore;
import org.hashsplit4j.api.HashStore;
import org.hibernate.Session;

/**
 *
 * @author brad
 */
public abstract class AbstractSpliffyCollectionResource extends AbstractSpliffyResource{
    /**
     * Called when a child has been changed, so that parent objects can
     * update their hashes
     * 
     * @param session
     * @return - the new repository version number
     */
    public abstract long save(Session session);
    
   
    public AbstractSpliffyCollectionResource(HashStore hashStore, BlobStore blobStore) {
        super(hashStore, blobStore);
    }    
}
