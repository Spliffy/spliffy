package org.spliffy.server.web.sharing;

import com.bradmcevoy.http.*;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.exceptions.NotFoundException;
import com.ettrema.http.acl.Principal;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.hibernate.Session;
import org.spliffy.server.db.*;
import org.spliffy.server.db.utils.SessionManager;
import org.spliffy.server.web.*;

/**
 * This is a resource representing the share, or share invitation
 *
 * @author brad
 */
public class ShareResource extends AbstractResource implements GetableResource, PostableResource {

    private final Share share;
    private final SpliffyCollectionResource parent;
    private JsonResult jsonResult;

    public ShareResource(Share link, SpliffyCollectionResource parent) {
        super(parent.getServices());
        this.share = link;
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
        
        String recipEntity = parameters.get("recipEntity");
        String sharedAsName = parameters.get("sharedAsName");
        try {
            Session session = SessionManager.session();
            Organisation rootOrg = Organisation.findRoot(session); 
            BaseEntity sharedTo = BaseEntity.find(rootOrg, recipEntity, session);
            getServices().getShareManager().acceptShare(getCurrentUser(), share, sharedTo, sharedAsName);
            String newPath = "/" + recipEntity + "/" + sharedAsName; // TODO: encoding
            jsonResult = new JsonResult(true, "Accepted ok", newPath);
        } catch (Exception ex) {
            jsonResult = new JsonResult(false, ex.getMessage());
        }
                
        return null;
    }

    @Override
    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException, NotFoundException {
        // TODO: We should show the same content as the page being shared
        if( jsonResult != null ) {
            jsonResult.write(out);
        } else {
            services.getTemplater().writePage("share.ftl", this, params, out, getCurrentUser());
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

    public Share getLink() {
        return share;
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
    public void addPrivs(List<Priviledge> list, Profile user) {
        parent.addPrivs(list, user);
    }

    @Override
    public String getName() {
        return share.getId().toString();
    }

    @Override
    public Date getModifiedDate() {
        return share.getAcceptedDate();
    }

    @Override
    public Date getCreateDate() {
        return share.getCreatedDate();
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

    public Share getShare() {
        return share;
    }

    @Override
    public Organisation getOrganisation() {
        return parent.getOrganisation();
    }
    
    
}
