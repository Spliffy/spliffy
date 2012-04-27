package org.spliffy.server.web;

/**
 *
 * @author brad
 */
public abstract class AbstractCollectionResource extends AbstractResource implements SpliffyCollectionResource{

       
    public AbstractCollectionResource(Services services) {
        super(services);
    }

    @Override
    public boolean isDir() {
        return true;
    }
    
    
}
