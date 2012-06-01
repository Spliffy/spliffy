package org.spliffy.server.web;

import com.bradmcevoy.http.DigestResource;
import com.ettrema.http.AccessControlledResource;
import java.util.List;
import org.spliffy.server.db.BaseEntity;
import org.spliffy.server.db.Organisation;
import org.spliffy.server.db.Profile;

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
    Profile getCurrentUser();

    /**
     * Get the organisation which most directly contains this resource
     * 
     * @return 
     */
    Organisation getOrganisation();
    
    /**
     * Add whatever permissions are defined on this resource for the given user
     * 
     * @param list
     * @param user 
     */
    void addPrivs(List<AccessControlledResource.Priviledge> list, Profile user);    
}
