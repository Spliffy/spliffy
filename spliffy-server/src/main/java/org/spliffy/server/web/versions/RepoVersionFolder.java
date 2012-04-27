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
import org.spliffy.server.db.*;
import org.spliffy.server.web.AbstractCollectionResource;
import org.spliffy.server.web.Services;
import org.spliffy.server.web.SpliffyCollectionResource;
import org.spliffy.server.web.Utils;

/**
 * Represents the complete state of a repository at some point in time
 *
 * @author brad
 */
public class RepoVersionFolder extends AbstractCollectionResource implements VersionCollectionResource, GetableResource{
    private final RepoVersion repoVersion;
    
    private final SpliffyCollectionResource parent;
    
    private List<AbstractVersionResource> children;

    public RepoVersionFolder(SpliffyCollectionResource parent, RepoVersion repoVersion, Services services) {
        super(services);
        this.parent = parent;
        this.repoVersion = repoVersion;
    }

    @Override
    public ItemVersion getItemVersion() {
        return null;
    }

    @Override
    public Date getCreateDate() {
        return repoVersion.getCreatedDate();
    }

    @Override
    public String getName() {
        return repoVersion.getVersionNum() + "";
    }

    @Override
    public Date getModifiedDate() {
        return repoVersion.getCreatedDate();
    }

    @Override
    public Resource child(String childName) throws NotAuthorizedException, BadRequestException {
        return Utils.childOf(getChildren(), childName);
    }

    @Override
    public List<? extends Resource> getChildren() throws NotAuthorizedException, BadRequestException {
        if (children == null) {
            if (repoVersion != null) {
                List<DirectoryMember> members = repoVersion.getRootItemVersion().getMembers();
                children = VersionUtils.toResources(this, members);
            } else {
                children = new ArrayList<>();
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
    public SpliffyCollectionResource getParent() {
        return parent;
    }

    @Override
    public BaseEntity getOwner() {
        return parent.getOwner();
    }

    @Override
    public void addPrivs(List<Priviledge> list, User user) {

    }

    

}
