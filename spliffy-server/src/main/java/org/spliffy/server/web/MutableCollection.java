package org.spliffy.server.web;

import com.bradmcevoy.http.CollectionResource;
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
public interface MutableCollection extends MutableResource, CollectionResource, MakeCollectionableResource {

    long save(Session session);

    void removeChild(MutableResource r) throws NotAuthorizedException, BadRequestException;

    void addChild(MutableResource r)throws NotAuthorizedException, BadRequestException;;
    
    /**
     * Called when the has on a child has been updated
     * 
     */
    void onChildChanged(MutableResource r);
    
    /**
     * Flag which indicates that some members have changed
     * 
     * @return 
     */
    boolean isDirty();

    public MutableCollection getParent();
    
}
