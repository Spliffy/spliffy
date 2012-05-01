package org.spliffy.server.web.sharing;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.*;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.exceptions.NotFoundException;
import com.ettrema.http.acl.Principal;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.spliffy.server.db.*;
import org.spliffy.server.web.*;

/**
 * This is a resource representing the share, or share invitation
 *
 * @author brad
 */
public class ShareResource extends AbstractResource implements GetableResource, PostableResource {

    private final Link link;
    private final SpliffyCollectionResource parent;
    private JsonResult jsonResult;

    public ShareResource(Link link, SpliffyCollectionResource parent) {
        super(parent.getServices());
        this.link = link;
        this.parent = parent;
    }

    /**
     * Process the share acceptance form. If successful this will result in a
     * new folder being created in the selected location (sharedTo parameter)
     * with the same content as the sharedFrom property of the Link object, and
     * the link object will ensure that both folders are kept in sync
     * 
     * 
     * Intended to be used with AJAX, so returns JSON
     *
     * @param parameters
     * @param files
     * @return
     * @throws BadRequestException
     * @throws NotAuthorizedException
     * @throws ConflictException
     */
    @Override
    public String processForm(Map<String, String> parameters, Map<String, FileItem> files) throws BadRequestException, NotAuthorizedException, ConflictException {
        Session session = SessionManager.session();
        Transaction tx = session.beginTransaction();

        // This assumes there is a current user
        User curUser = getCurrentUser();
        if (curUser == null) {
            jsonResult = new JsonResult(false, "Please login");
            return null;
        }
        List<Repository> repos = curUser.getRepositories();
        if (repos == null) {
            repos = new ArrayList<>();
            curUser.setRepositories(repos);
        }
        String sPath = parameters.get("sharedTo"); // relative to the user
        Path path = Path.path(sPath);
        System.out.println("Accept on path: " + path);
        UserResource userResource = (UserResource) SpliffyResourceFactory.getRootFolder().findEntity(curUser.getName());
        CollectionResource col = findCol(userResource, path);
        if( col instanceof MutableCollection ) {
            MutableCollection mCol = (MutableCollection) col;
            Item sharedTo = mCol.getItemVersion().getItem();
            link.setAcceptedDate(new Date());
            link.setCreatedDate(new Date()); // todo: remove
            link.setSharedTo(sharedTo);
            session.save(link);
            
            Path newPath = Path.path(curUser.getName());
            newPath = newPath.add(path);
            jsonResult = new JsonResult(true, "Accepted ok", newPath.toString());
        } else {
            jsonResult = new JsonResult(false, "The specified path does not point to a valid folder: " + path);
        }
        
        tx.commit();
        return null;
    }

    @Override
    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException, NotFoundException {
        // TODO: We should show the same content as the page being shared
        if( jsonResult != null ) {
            jsonResult.write(out);
        } else {
            services.getTemplater().writePage("share.ftl", this, params, out);
        }
    }

    @Override
    public boolean authorise(Request request, Method method, Auth auth) {
        // if the user wants to accept as an existing user they will click a link like (shareId)?login
        // So this authorise method will force a login, so that there is a current user to accept as
        if (request.getParams().containsKey("login")) {
            return auth != null && auth.getTag() != null;
        } else {
            return true;
        }
    }

    public Link getLink() {
        return link;
    }

    @Override
    public boolean isDir() {
        return false;
    }

    @Override
    public SpliffyCollectionResource getParent() {
        return parent;
    }

    @Override
    public BaseEntity getOwner() {
        return null;
    }

    @Override
    public void addPrivs(List<Priviledge> list, User user) {
        parent.addPrivs(list, user);
    }

    @Override
    public String getName() {
        return link.getId().toString();
    }

    @Override
    public Date getModifiedDate() {
        return link.getAcceptedDate();
    }

    @Override
    public Date getCreateDate() {
        return link.getCreatedDate();
    }

    @Override
    public Long getMaxAgeSeconds(Auth auth) {
        return null;
    }

    @Override
    public String getContentType(String accepts) {
        if( jsonResult != null ) {
            return "application/x-javascript; charset=utf-8";
        }
        return "text/html";
    }

    @Override
    public Long getContentLength() {
        return null;
    }

    @Override
    public Map<Principal, List<Priviledge>> getAccessControlList() {
        return null;
    }

    private CollectionResource findCol(UserResource userResource, Path path) throws NotAuthorizedException, BadRequestException {
        if (path.isRoot()) {
            return userResource;
        } else {
            CollectionResource parent = findCol(userResource, path.getParent());
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
}
