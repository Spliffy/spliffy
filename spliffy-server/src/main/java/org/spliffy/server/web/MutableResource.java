package org.spliffy.server.web;

import org.spliffy.server.db.ItemVersion;

/**
 * Represents a web resource which can be changed (ie is mutable)
 * 
 * Being mutable is has a hash value
 *
 * @author brad
 */
public interface MutableResource extends SpliffyResource {

    /**
     * Flag which indicates that this resource or its members (if a directory) have changed
     * 
     * @return 
     */
    boolean isDirty();    
           
    Long getEntryHash();
    
    ItemVersion getItemVersion();

    /**
     * Called during the save process. Must reset dirty flag!!!
     * 
     * @param newVersion 
     */
    void setItemVersion(ItemVersion newVersion);
    
    
    public String getType();
}
