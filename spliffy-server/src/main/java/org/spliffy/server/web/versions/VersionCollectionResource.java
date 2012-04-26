package org.spliffy.server.web.versions;

import com.bradmcevoy.http.CollectionResource;
import org.spliffy.server.web.Services;

/**
 * Represents both the RepoVersionFolder and DirectoryVersionResource
 * so we can have a common interface for parent objects
 *
 * @author brad
 */
public interface VersionCollectionResource extends CollectionResource {
    Services getServices();
}
