package org.spliffy.common;

import java.util.List;
import java.util.UUID;

/**
 * Represents an entry in the filesystem, with a name, a hash and a 
 * unique meta ID
 *
 * @author brad
 */
public class FileTriplet {
    private String name;
    private long hash;
    private UUID metaId;
    private String type;
    
    private List<FileTriplet> children;

    public long getHash() {
        return hash;
    }

    public void setHash(long hash) {
        this.hash = hash;
    }

    public UUID getMetaId() {
        return metaId;
    }

    public void setMetaId(UUID metaId) {
        this.metaId = metaId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<FileTriplet> getChildren() {
        return children;
    }

    public void setChildren(List<FileTriplet> children) {
        this.children = children;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
        
    public boolean isDirectory() {
        return type != null && type.equals("d"); // d for directory
    }
    
    
}
