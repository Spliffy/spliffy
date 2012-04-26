package org.spliffy.server.web;

import org.hibernate.Session;

/**
 *
 * @author brad
 */
public abstract class AbstractSpliffyCollectionResource extends AbstractSpliffyResource{

       
    public AbstractSpliffyCollectionResource(Services services) {
        super(services);
    }

    @Override
    public boolean isDir() {
        return true;
    }
    
    
}
