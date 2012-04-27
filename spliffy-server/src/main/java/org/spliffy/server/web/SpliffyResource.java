package org.spliffy.server.web;

import com.bradmcevoy.http.DigestResource;
import org.spliffy.server.db.BaseEntity;
import org.spliffy.server.db.User;

/**
 * Common interface for all spliffy resources
 *
 * @author brad
 */
public interface SpliffyResource extends DigestResource{
    SpliffyCollectionResource getParent();
    
    Services getServices();
    
    /**
     * Find whatever entity (user or other) which owns the given resource
     * 
     * @return 
     */
    BaseEntity getOwner();
    
    /**
     * Returns the current user on this request
     * 
     * @return 
     */
    User getCurrentUser();
}
