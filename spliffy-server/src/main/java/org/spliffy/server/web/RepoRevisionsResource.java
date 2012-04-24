package org.spliffy.server.web;

import com.bradmcevoy.http.*;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.exceptions.NotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.spliffy.server.db.Repository;

/**
 *
 * @author brad
 */
public class RepoRevisionsResource implements CollectionResource{

    private final Repository repository;

    public RepoRevisionsResource(Repository repository) {
        this.repository = repository;
    }
    

    @Override
    public String getUniqueId() {
        return null;
    }

    @Override
    public String getName() {
        return "revisions";
    }

    @Override
    public Object authenticate(String user, String password) {
        return user; // TODO
    }

    @Override
    public boolean authorise(Request request, Method method, Auth auth) {
        return true; // TODO
    }

    @Override
    public String getRealm() {
        return "spliffy"; //TODO
    }

    @Override
    public Date getModifiedDate() {
        return null;
    }

    @Override
    public String checkRedirect(Request request) {
        return null;
    }

    @Override
    public Resource child(String childName) throws NotAuthorizedException, BadRequestException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<? extends Resource> getChildren() throws NotAuthorizedException, BadRequestException {
        return Collections.EMPTY_LIST;
    }

}
