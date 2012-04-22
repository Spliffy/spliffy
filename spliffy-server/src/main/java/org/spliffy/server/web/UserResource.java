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
public class UserResource extends AbstractSpliffyResource implements CollectionResource, MakeCollectionableResource, PropFindableResource {

    private final User user;
    private final VersionNumberGenerator versionNumberGenerator;

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
        List<RepoResource> list = new ArrayList();
        if (user.getRepositories() != null) {
            for (Repository r : user.getRepositories()) {
                RepoResource rr = new RepoResource(r, hashStore, blobStore, versionNumberGenerator);
                list.add(rr);
            }
        }
        return list;
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
        return new RepoResource(r, hashStore, blobStore, versionNumberGenerator);
    }

    @Override
    public void onChildChanged(Session session) {
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
