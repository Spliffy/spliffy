package org.spliffy.server.web;

import com.bradmcevoy.http.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.spliffy.server.db.DirEntry;
import org.spliffy.server.db.MiltonOpenSessionInViewFilter;
import org.spliffy.server.db.ResourceMeta;

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

    public static AbstractSpliffyResource toResource(AbstractSpliffyResource parent, DirEntry de) {
        UUID metaId = de.getMetaId();
        ResourceMeta meta = ResourceMeta.find(metaId);
        String type = meta.getType();
        if (type.equals("d")) {
            RepoDirectoryResource rdr = new RepoDirectoryResource(de.getName(),meta, parent, parent.getHashStore(), parent.getBlobStore());
            rdr.setDirHash(de.getEntryHash());
            return rdr;
        } else {
            RepoDirectoryResource parentDir = (RepoDirectoryResource) parent;
            RepoFileResource rfr = new RepoFileResource(de.getName(), meta, parentDir, parent.getHashStore(), parent.getBlobStore());
            rfr.setHash(de.getEntryHash());
            return rfr;
        }
    }

    public static List<AbstractSpliffyResource> toResources(AbstractSpliffyResource parent, List<DirEntry> dirEntries) {
        List<AbstractSpliffyResource> list = new ArrayList<>();
        for (DirEntry de : dirEntries) {
            AbstractSpliffyResource r = Utils.toResource(parent, de);
            list.add(r);
        }
        return list;
    }

    public static ResourceMeta newDirMeta() {
        ResourceMeta meta = new ResourceMeta();
        meta.setId(UUID.randomUUID());
        meta.setType("d");
        meta.setCreateDate(new Date());
        meta.setModifiedDate(new Date());
        MiltonOpenSessionInViewFilter.session().save(meta);
        return meta;
    }
    
    public static ResourceMeta newFileMeta() {
        ResourceMeta meta = new ResourceMeta();
        meta.setId(UUID.randomUUID());
        meta.setType("f");
        meta.setCreateDate(new Date());
        meta.setModifiedDate(new Date());
        MiltonOpenSessionInViewFilter.session().save(meta);
        return meta;
    }    
}
