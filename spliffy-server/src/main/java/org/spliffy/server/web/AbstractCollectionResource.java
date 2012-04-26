package org.spliffy.server.web;

import com.bradmcevoy.http.CollectionResource;

/**
 *
 * @author brad
 */
public abstract class AbstractCollectionResource extends AbstractResource implements CollectionResource{

       
    public AbstractCollectionResource(Services services) {
        super(services);
    }

    @Override
    public boolean isDir() {
        return true;
    }
    
    
}
