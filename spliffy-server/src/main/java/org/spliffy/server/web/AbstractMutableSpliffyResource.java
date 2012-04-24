package org.spliffy.server.web;

import com.bradmcevoy.http.*;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import java.util.Date;
import java.util.UUID;
import org.hashsplit4j.api.*;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.spliffy.server.db.DeletedItem;
import org.spliffy.server.db.MiltonOpenSessionInViewFilter;
import org.spliffy.server.db.RepoVersion;
import org.spliffy.server.db.ResourceVersionMeta;

/**
 *
 * @author brad
 */
public abstract class AbstractMutableSpliffyResource extends AbstractSpliffyResource implements PropFindableResource, GetableResource, DeletableResource, MutableResource, CopyableResource, MoveableResource {

    protected String name;
    protected final MutableCollection parent;
    protected ResourceVersionMeta meta;
    protected long hash;

    public AbstractMutableSpliffyResource(String name, ResourceVersionMeta meta, MutableCollection parent, HashStore hashStore, BlobStore blobStore) {
        super(hashStore, blobStore);
        this.meta = meta;
        this.name = name;
        this.parent = parent;
    }

    @Override
    public void moveTo(CollectionResource rDest, String newName) throws ConflictException, NotAuthorizedException, BadRequestException {

        if (!(rDest instanceof MutableCollection)) {
            throw new ConflictException(this, "Can't move to: " + rDest.getClass());
        }
        MutableCollection newParent = (MutableCollection) rDest;

        Session session = MiltonOpenSessionInViewFilter.session();
        Transaction tx = session.beginTransaction();


        if (newParent.getMetaId().equals(parent.getMetaId())) {
            // just rename
            this.name = newName;
            parent.onChildChanged(this);
            parent.save(session);
        } else {
            parent.removeChild(this);
            newParent.addChild(this);
            newParent.save(session); // save calls up to RepoResource which will call back down to save dirty nodes, including old parent
        }

        tx.commit();
    }

    @Override
    public void delete() throws NotAuthorizedException, ConflictException, BadRequestException {
        Session session = MiltonOpenSessionInViewFilter.session();
        Transaction tx = session.beginTransaction();

        DeletedItem deletedItem = new DeletedItem();
        deletedItem.setId(UUID.randomUUID());
        if (parent.getMetaId() != null) { // will be null for folders directly in a RepoVersion
            ResourceVersionMeta deletedFrom = ResourceVersionMeta.find(parent.getMetaId());
            deletedItem.setDeletedFrom(deletedFrom);

        }
        ResourceVersionMeta deletedResource = ResourceVersionMeta.find(getMetaId());
        deletedItem.setDeletedResource(deletedResource);
        deletedItem.setRepoVersion(currentRepoVersion());
        session.save(deletedItem);

        parent.removeChild(this);
        parent.save(session);
        tx.commit();
    }

    @Override
    public Date getCreateDate() {
        return meta.getResourceMeta().getCreateDate();
    }

    @Override
    public Date getModifiedDate() {
        return meta.getModifiedDate();
    }

    @Override
    public String getUniqueId() {
        return null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Long getEntryHash() {
        return hash;
    }

    public void setHash(long hash) {
        this.hash = hash;
    }

    @Override
    public UUID getMetaId() {
        return meta.getId();
    }

    @Override
    public Long getMaxAgeSeconds(Auth auth) {
        return null;
    }

    public RepoVersion currentRepoVersion() {
        MutableCollection col = parent;
        while (!(col instanceof RepoResource)) {
            col = col.getParent();
        }
        RepoResource rr = (RepoResource) col;
        return rr.getRepoVersion();
    }
}
