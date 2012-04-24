package org.spliffy.server.web;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.MakeCollectionableResource;
import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.hashsplit4j.api.BlobStore;
import org.hashsplit4j.api.HashStore;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.spliffy.server.db.*;

/**
 *
 * @author brad
 */
public class UserResource extends AbstractSpliffyCollectionResource implements CollectionResource, MakeCollectionableResource, PropFindableResource {

    private final User user;
    private final VersionNumberGenerator versionNumberGenerator;
    private List<RepoResource> children;

    public UserResource(User u, HashStore hashStore, BlobStore blobStore, VersionNumberGenerator versionNumberGenerator) {
        super(hashStore, blobStore);
        this.user = u;
        this.versionNumberGenerator = versionNumberGenerator;

    }

    @Override
    public Resource child(String childName) throws NotAuthorizedException, BadRequestException {
        return Utils.childOf(getChildren(), childName);
    }

    @Override
    public List<? extends Resource> getChildren() throws NotAuthorizedException, BadRequestException {
        if( children == null ) {
            children = new ArrayList();
            if (user.getRepositories() != null) {
                for (Repository r : user.getRepositories()) {
                    RepoVersion rv = r.latestVersion();
                    RepoResource rr = new RepoResource(r, rv, hashStore, blobStore, versionNumberGenerator);
                    children.add(rr);
                }
            }
        }
        return children;
    }

    @Override
    public String getName() {
        return user.getName();
    }

    @Override
    public CollectionResource createCollection(String newName) throws NotAuthorizedException, ConflictException, BadRequestException {
        Transaction tx = MiltonOpenSessionInViewFilter.session().beginTransaction();
        Repository r = new Repository();
        r.setId(UUID.randomUUID());
        r.setUser(user);
        r.setName(newName);
        r.setVersions(new ArrayList<RepoVersion>());
        r.setCreatedDate(new Date());
        List<Repository> list = user.getRepositories();
        if (list == null) {
            list = new ArrayList<>();
        }
        list.add(r);
        MiltonOpenSessionInViewFilter.session().save(r);
        tx.commit();
        RepoVersion rv = r.latestVersion();
        return new RepoResource(r, rv,hashStore, blobStore, versionNumberGenerator);
    }

    @Override
    public long save(Session session) {
        throw new RuntimeException("Not supported");
    }

    @Override
    public Long getEntryHash() {
        return null;
    }

    @Override
    public UUID getMetaId() {
        return null;
    }

    @Override
    public Date getCreateDate() {
        return user.getCreatedDate();
    }

    @Override
    public Date getModifiedDate() {
        return user.getModifiedDate();
    }
}
