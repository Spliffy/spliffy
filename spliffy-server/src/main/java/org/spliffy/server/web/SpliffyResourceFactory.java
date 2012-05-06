package org.spliffy.server.web;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.*;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.exceptions.NotFoundException;
import com.bradmcevoy.http.http11.auth.DigestResponse;
import com.bradmcevoy.http.webdav.PropertySourcesList;
import com.ettrema.common.Service;
import com.ettrema.event.EventManager;
import com.ettrema.http.AccessControlledResource.Priviledge;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import org.spliffy.server.apps.ApplicationManager;
import org.spliffy.server.db.*;

/**
 *
 * @author brad
 */
public class SpliffyResourceFactory implements ResourceFactory, Service {

    public static RootFolder getRootFolder() {
        return (RootFolder) HttpManager.request().getAttributes().get("_spliffy_root_folder");
    }
    
    private final UserDao userDao;    
    private final VersionNumberGenerator versionNumberGenerator;
    private final SpliffySecurityManager securityManager;
    private final Services services;
    private final ApplicationManager applicationManager;
    private final EventManager eventManager;
    private final PropertySourcesList propertySources;

    public SpliffyResourceFactory(UserDao userDao, VersionNumberGenerator versionNumberGenerator, SpliffySecurityManager securityManager, Services services, ApplicationManager applicationManager, EventManager eventManager, PropertySourcesList propertySources) {
        this.userDao = userDao;
        this.versionNumberGenerator = versionNumberGenerator;
        this.securityManager = securityManager;
        this.services = services;
        this.applicationManager = applicationManager;
        this.eventManager = eventManager;
        this.propertySources = propertySources;
    }
    

    @Override
    public void start() {
        applicationManager.init(this);
    }

    @Override
    public void stop() {
        applicationManager.shutDown();
    }
   
    public RootFolder createRootFolder() {
        return new RootFolder();
    }
    
    @Override
    public Resource getResource(String host, String sPath) throws NotAuthorizedException, BadRequestException {
        Path path = Path.path(sPath);
        Resource r = find(host, path);
        return r;
    }

    private Resource find(String host, Path p) throws NotAuthorizedException, BadRequestException {        
        if (p.isRoot()) {
            RootFolder rootFolder = (RootFolder) HttpManager.request().getAttributes().get("_spliffy_root_folder");
            if( rootFolder == null ) {
                rootFolder = new RootFolder();
                HttpManager.request().getAttributes().put("_spliffy_root_folder", rootFolder);
            }
            return rootFolder;
        } else {
            Resource rParent = find(host, p.getParent());
            if (rParent == null) {
                return null;
            } else {
                if (rParent instanceof CollectionResource) {
                    CollectionResource parent = (CollectionResource) rParent;
                    return parent.child(p.getName());
                } else {
                    return null;
                }
            }
        }
    }

    public class RootFolder implements SpliffyCollectionResource, GetableResource, PropFindableResource {

        private Map<String,PrincipalResource> children = new HashMap<>();
        
        protected User currentUser;        
        
        @Override
        public String getUniqueId() {
            return null;
        }

        @Override
        public String getName() {
            return "";
        }

        @Override
        public Object authenticate(String user, String password) {
            currentUser = (User) securityManager.authenticate(user, password);
            return currentUser;
        }
        
        @Override
        public Object authenticate(DigestResponse digestRequest) {
            currentUser = (User) securityManager.authenticate(digestRequest);
            return currentUser;
        }        

        @Override
        public boolean authorise(Request request, Request.Method method, Auth auth) {
            return true;
        }

        @Override
        public String getRealm() {
            return securityManager.getRealm();
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
            Resource r = applicationManager.getNonBrowseablePage(this, childName);
            if( r != null ) {
                return r;
            }
            return findEntity(childName);
        }
        
        public PrincipalResource findEntity(String name) {
            PrincipalResource r = children.get(name);
            if( r != null ) {
                return r;
            }
            User u = userDao.getUser(name);
            if (u == null) {
                return null;
            } else {
                UserResource ur = new UserResource(this, u, versionNumberGenerator, applicationManager);
                children.put(name, ur);
                return ur;
            }            
        }
        

        @Override
        public List<? extends Resource> getChildren() throws NotAuthorizedException, BadRequestException {
            return Collections.EMPTY_LIST; // browsing not supported
        }

        @Override
        public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException, NotFoundException {
            services.getTemplater().writePage("home.ftl", this, params, out, getCurrentUser());
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
            return null;
        }

        @Override
        public Services getServices() {
            return services;
        }

        @Override
        public boolean isDigestAllowed() {
            return true;
        }

        @Override
        public BaseEntity getOwner() {
            return null;
        }

        @Override
        public User getCurrentUser() {
            return currentUser;
        }

        @Override
        public void addPrivs(List<Priviledge> list, User user) {

        }

        @Override
        public Date getCreateDate() {
            return null;
        }
    }

    public ApplicationManager getApplicationManager() {
        return applicationManager;
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    public PropertySourcesList getPropertySources() {
        return propertySources;
    }

    public SpliffySecurityManager getSecurityManager() {
        return securityManager;
    }

    public Services getServices() {
        return services;
    }

    public UserDao getUserDao() {
        return userDao;
    }

    public VersionNumberGenerator getVersionNumberGenerator() {
        return versionNumberGenerator;
    }

    
}
