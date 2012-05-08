package org.spliffy.server.manager;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import java.util.Date;
import java.util.List;
import org.hibernate.Session;
import org.spliffy.server.db.*;
import org.spliffy.server.web.*;

/**
 *
 * @author brad
 */
public class ResourceManager {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ResourceManager.class);
    private final VersionNumberGenerator versionNumberGenerator;

    public ResourceManager(VersionNumberGenerator versionNumberGenerator) {
        this.versionNumberGenerator = versionNumberGenerator;
    }

    public CollectionResource findCol(CollectionResource root, Path path) throws NotAuthorizedException, BadRequestException {
        if (path.isRoot()) {
            return root;
        } else {
            CollectionResource parent = findCol(root, path.getParent());
            if( parent == null ) {
                System.out.println("Couldnt find parent: " + path.getParent().getName());
                return null;
            } else {
                Resource r = parent.child(path.getName());
                if( r == null ) {
                    System.out.println("Couldnt find child: " + path.getName() + " of " + parent.getName());
                    return null;
                } else {
                    if( r instanceof CollectionResource ) {
                        return (CollectionResource) r;
                    } else {
                        System.out.println("Found a resource which is not a mutablecollection: " + r.getClass() + " - " + r.getName());
                        return null;
                    }
                }
            }
        }
    }

    
    public void save(Session session, RepositoryFolder repositoryFolder) {
        log.trace("save repo folder: " + repositoryFolder.getName() + "dirty=" + repositoryFolder.isDirty());
        if (!repositoryFolder.isDirty()) {
            return;
        }

        Repository repo = getRepoToUse(repositoryFolder.getDirectRepository());
        try {
            for (MutableResource r : repositoryFolder.getChildren()) { // if is dirty then children must be loaded
                if (r instanceof MutableCollection) {
                    MutableCollection col = (MutableCollection) r;
                    calcHashes(session, col); // each collection checks its own dirty flag, won't do anything if clean
                }
            }

            ItemVersion newVersion = Utils.newItemVersion(repositoryFolder.getItemVersion(), repositoryFolder.getType());
            repositoryFolder.setItemVersion(newVersion);
            log.trace("Inserted new root item version id: " + newVersion.getId() + " for: " + repositoryFolder.getName());

            saveCollection(session, repositoryFolder);

            RepoVersion newRepoVersion = new RepoVersion();
            newRepoVersion.setCreatedDate(new Date());
            newRepoVersion.setRepository(repo); // not direct repo
            newRepoVersion.setRootItemVersion(repositoryFolder.getRootItemVersion());
            long newVersionNum = versionNumberGenerator.nextVersionNumber(repositoryFolder.getDirectRepository());
            newRepoVersion.setVersionNum(newVersionNum);
            session.save(newRepoVersion);
            repo.setHead(newRepoVersion);
            session.save(repo);

        } catch (NotAuthorizedException | BadRequestException ex) {
            throw new RuntimeException(ex);
        }
    }
    

    /**
     * Check if this is dirty, and if so recalculate the hash for the directory
     *
     * A recursive call, recalculating children as necessary
     *
     * @param session
     */
    public void calcHashes(Session session, MutableCollection parent) throws NotAuthorizedException, BadRequestException {
        log.trace("calcHashes: " + parent.getName() + " dirty=" + parent.isDirty() + " hashcode: " + parent.hashCode());
        if (!parent.isDirty()) {
            return;
        }
        List<MutableResource> nextChildren = (List<MutableResource>) parent.getChildren();
        for (MutableResource r : nextChildren) { // if is dirty then children must be loaded
            if (r instanceof MutableCollection) {
                MutableCollection col = (MutableCollection) r;
                calcHashes(session, col);
            }
        }
        // calc new dirHash and save dir entries for children
        long newHash = HashCalc.calcResourceesHash(nextChildren);
        parent.setEntryHash(newHash);
        return;
    }

    private void saveCollection(Session session, MutableCollection parent) throws NotAuthorizedException, BadRequestException {
        if (!parent.isDirty()) {
            log.trace("saveCollection: " + parent.getName() + " not dirty");
            return;
        } else {
            log.trace("saveCollection: " + parent.getName());
        }

        List<MutableResource> nextChildren = (List<MutableResource>) parent.getChildren();
        for (MutableResource r : nextChildren) { // if is dirty then children must be loaded
            insertMember(session, parent.getItemVersion(), r);
        }
        parent.setDirty(false);
    }

    /**
     * If the resource has itself changed, then create a new ItemVersion for
     * itself
     *
     * Create a new DirectoryMember to connect the ItemVersion of the member to
     * the new parent version.
     *
     *
     * @param session
     * @param parentItemVersion
     * @param r
     */
    private void insertMember(Session session, ItemVersion parentItemVersion, MutableResource r) throws NotAuthorizedException, BadRequestException {
        // The item version of this directory member before it was updated
        ItemVersion origMemberIV = r.getItemVersion();
        DirectoryMember origMemberDM = r.getDirectoryMember();

        ItemVersion newMemberIV;
        if (r.isDirty()) {
            newMemberIV = Utils.newItemVersion(r.getItemVersion(), r.getType()); // create a new ItemVersion for the member
        } else {
            newMemberIV = r.getItemVersion(); // use existing ItemVersion
        }
        newMemberIV.setItemHash(r.getEntryHash());
        r.setItemVersion(newMemberIV);
        DirectoryMember newMemberDM = new DirectoryMember();
        newMemberDM.setParentItem(parentItemVersion);
        newMemberDM.setName(r.getName());
        log.trace("created new DirectoryMember: " + newMemberDM.getName());

        newMemberDM.setMemberItem(newMemberIV);
        session.save(newMemberDM);


        if (r instanceof MutableCollection) {
            MutableCollection col = (MutableCollection) r;
            saveCollection(session, col); // will do dirty check
        }

        updateLinked(origMemberIV, newMemberIV, origMemberDM, newMemberDM, session);
    }

    /**
     * Called when a new version of an item has been created. Should find all
     * other instances of that IV (ie DirectoryMembers other then the one given)
     * which are linked to the old version of the IV by its member link (ie not
     * as children with it as parent)
     *
     *
     * @param origMemberIV - this is the IV which might be shared
     * @param newMemberIV - the new version which contains updated members
     * @param newMemberDM - this is the DM of the resource which has already
     * been updated
     */
    private void updateLinked(ItemVersion origMemberIV, ItemVersion newMemberIV, DirectoryMember origMemberDM, DirectoryMember newMemberDM, Session session) {
        // we already created newMemberIV and this has been connected to one parent. But
        // we need to check for other parents on origMemberIV and ensure they have new
        // versions created which link to the newMemberIV
        List<DirectoryMember> linked = origMemberIV.getLinked();
        if (linked != null) {
            for (DirectoryMember siblingDM : linked) {
                if (origMemberDM == null || origMemberDM != siblingDM) {
                    // is a DM other then the one updated
                    siblingDM.updateTo(newMemberIV, session);
                }
            }
        }
    }

    /**
     * Find the latest version of the given repository, following links if needed
     * 
     * @param r
     * @return 
     */
    public RepoVersion getHead(Repository r) {
        Repository toUse = getRepoToUse(r);
        return toUse.latestVersion();
    }
    
    public Repository getRepoToUse(Repository r) {
        Repository linked = r.getLinkedTo();
        if( linked != null ) {
            return getRepoToUse(linked);
        } else {
            return r;
        }
    }    
}
