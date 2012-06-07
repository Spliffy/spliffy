package org.spliffy.server.apps.versions;

import com.bradmcevoy.http.*;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.exceptions.NotFoundException;
import com.ettrema.http.acl.Principal;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.spliffy.server.db.BaseEntity;
import org.spliffy.server.db.Organisation;
import org.spliffy.server.db.Repository;
import org.spliffy.server.db.Profile;
import org.spliffy.server.web.AbstractCollectionResource;
import org.spliffy.server.web.Services;
import org.spliffy.server.web.SpliffyCollectionResource;
import org.spliffy.server.web.Utils;

/**
 *
 * @author brad
 */
public class VersionsRootFolder extends  AbstractCollectionResource implements GetableResource{

    private final BaseEntity baseEntity;
    
    private final SpliffyCollectionResource parent;
    
    private List<RepositoryVersionsFolder> children;

    public VersionsRootFolder(SpliffyCollectionResource parent, BaseEntity baseEntity, Services services) {
        super(services);
        this.parent = parent;
        this.baseEntity = baseEntity;
    }

    
    @Override
    public Resource child(String childName) throws NotAuthorizedException, BadRequestException {
        return Utils.childOf(getChildren(), childName);
    }
    
    
    @Override
    public List<? extends Resource> getChildren() throws NotAuthorizedException, BadRequestException {
        if (children == null) {
            children = new ArrayList();
            if (baseEntity.getRepositories() != null) {
                for (Repository r : baseEntity.getRepositories()) {
                    RepositoryVersionsFolder rr = new RepositoryVersionsFolder(parent, r, services);
                    children.add(rr);
                }
            }
        }
        return children;
    }
       
    @Override
    public Date getCreateDate() {
        return baseEntity.getCreatedDate();
    }

    @Override
    public String getName() {
        return "versions";
    }

    @Override
    public Date getModifiedDate() {
        return baseEntity.getModifiedDate();
    }

    @Override
    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException, NotFoundException {
        getTemplater().writePage("versionsHome", this, params, out);
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
        return baseEntity;
    }

    @Override
    public void addPrivs(List<Priviledge> list, Profile user) {
        parent.addPrivs(list, user);
    }
    
    @Override
    public Map<Principal, List<Priviledge>> getAccessControlList() {
        return null;
    }
    @Override
    public Organisation getOrganisation() {
        return parent.getOrganisation();
    }    
}
