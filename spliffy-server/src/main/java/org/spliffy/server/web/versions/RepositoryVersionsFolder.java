package org.spliffy.server.web.versions;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.exceptions.NotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.spliffy.server.db.ItemVersion;
import org.spliffy.server.db.RepoVersion;
import org.spliffy.server.db.Repository;
import org.spliffy.server.web.AbstractCollectionResource;
import org.spliffy.server.web.Services;
import org.spliffy.server.web.Utils;

/**
 * Lists all of the versions within a repository
 * 
 * TODO: refactor this to group versions to make it more manageable. Perhaps
 * groups by month, user, etc
 *
 * @author brad
 */
public class RepositoryVersionsFolder extends AbstractCollectionResource implements VersionCollectionResource, GetableResource{
    private final Repository repo;
    private final Resource parent;
    private List<RepoVersionFolder> children;

    public RepositoryVersionsFolder(Resource parent, Repository repo, Services services) {
        super(services);
        this.parent = parent;
        this.repo = repo;
    }

    @Override
    public List<? extends Resource> getChildren() throws NotAuthorizedException, BadRequestException {
        if (children == null) {
            children = new ArrayList<>();
            for( RepoVersion rv : repo.getVersions()) {
                RepoVersionFolder f = new RepoVersionFolder(this, rv, services);
                children.add(f);
            }
        }
        return children;
    }
    
    @Override
    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException, NotFoundException {
        getTemplater().writePage("repoVersion.ftl", this, params, out);
    }

    @Override
    public Long getMaxAgeSeconds(Auth auth) {
        return null;
    }

    @Override
    public String getContentType(String accepts) {
        return "text/html";
    }

    @Override
    public Long getContentLength() {
        return null;
    }    
    
    @Override
    public ItemVersion getItemVersion() {
        return null;
    }

    @Override
    public Date getCreateDate() {
        return repo.getCreatedDate();
    }

    @Override
    public String getName() {
        return repo.getName();
    }

    @Override
    public Date getModifiedDate() {
        return repo.getCreatedDate();
    }

    @Override
    public Resource child(String childName) throws NotAuthorizedException, BadRequestException {
        return Utils.childOf(getChildren(), childName);
    }

    public Resource getParent() {
        return parent;
    }
    
    
}
