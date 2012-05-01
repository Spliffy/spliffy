package org.spliffy.server.web;

import com.bradmcevoy.http.MakeCollectionableResource;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import org.hibernate.Session;

/**
 * Used for parent references. The parent can be either a RepoResource or
 * a RepoDirectoryResource
 *
 * @author brad
 */
public interface MutableCollection extends MutableResource, SpliffyCollectionResource, MakeCollectionableResource {

    void save(Session session);

    void removeChild(MutableResource r) throws NotAuthorizedException, BadRequestException;

    void addChild(MutableResource r)throws NotAuthorizedException, BadRequestException;;
    
    /**
     * Called when the has on a child has been updated
     * 
     */
    void onChildChanged(MutableResource r);
        
    /**
     * Called during the save procedure, the system will recalculate the
     * hash for this directory and set it here. Then it will be used to
     * create a new ItemVersion if necessary
     * 
     * @param newHash 
     */
    void setEntryHash(long newHash);

    public void setDirty(boolean b);


    
}
