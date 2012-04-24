package org.spliffy.server.web;

import com.bradmcevoy.http.Resource;
import java.util.*;
import org.spliffy.server.db.DirEntry;
import org.spliffy.server.db.MiltonOpenSessionInViewFilter;
import org.spliffy.server.db.ResourceMeta;
import org.spliffy.server.db.ResourceVersionMeta;

/**
 *
 * @author brad
 */
public class Utils {

    public static Resource childOf(List<? extends Resource> children, String name) {
        if (children == null) {
            return null;
        }
        for (Resource r : children) {
            if (r.getName().equals(name)) {
                return r;
            }
        }
        return null;
    }

    public static MutableResource toResource(MutableCollection parent, DirEntry de) {
        UUID metaId = de.getMetaId();
        ResourceVersionMeta meta = ResourceVersionMeta.find(metaId); 
        String type = meta.getResourceMeta().getType();
        switch (type) {
            case "d":
                RepoDirectoryResource rdr = new RepoDirectoryResource(de.getName(),meta, parent, parent.getHashStore(), parent.getBlobStore());
                rdr.setHash(de.getEntryHash());
                return rdr;
            case "f":
                RepoFileResource rfr = new RepoFileResource(de.getName(), meta, parent, parent.getHashStore(), parent.getBlobStore());
                rfr.setHash(de.getEntryHash());
                return rfr;
            default:
                throw new RuntimeException("Unknown resource type: " + type);
        }
    }

    public static List<MutableResource> toResources(MutableCollection parent, List<DirEntry> dirEntries) {
        List<MutableResource> list = new ArrayList<>();
        Set<String> names = new HashSet<>();
        for (DirEntry de : dirEntries) {
            String name = de.getName();
            if( names.contains(name )) {
                throw new RuntimeException("Name not unique within collection: " + name);
            }
            names.add(name);                    
            
            MutableResource r = Utils.toResource(parent, de);
            list.add(r);
        }
        return list;
    }

    public static ResourceVersionMeta newDirMeta() {
        return newMeta(null, "d");
    }
    
    public static ResourceVersionMeta newFileMeta() {
        return newMeta(null, "f");
    }    

    public static ResourceVersionMeta newFileMeta(ResourceMeta meta) {
        return newMeta(meta, "f");
    }    
    
    private static ResourceVersionMeta newMeta(ResourceMeta meta, String type) {
        if( meta == null ) {
            meta = new ResourceMeta();
            meta.setId(UUID.randomUUID());
            meta.setType(type);
            meta.setCreateDate(new Date());
        }
        
        ResourceVersionMeta versionMeta = new ResourceVersionMeta();
        versionMeta.setId(UUID.randomUUID());
        versionMeta.setModifiedDate(new Date());
        versionMeta.setResourceMeta(meta);
        
        List<ResourceVersionMeta> list = new ArrayList<>();
        list.add(versionMeta);
        meta.setVersions(list);
        
        MiltonOpenSessionInViewFilter.session().save(meta);
        return versionMeta;
    }    
}
