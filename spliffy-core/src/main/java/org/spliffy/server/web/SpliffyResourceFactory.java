package org.spliffy.server.web;

import org.spliffy.server.db.utils.UserDao;
import org.spliffy.server.db.utils.SessionManager;
import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.*;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.webdav.PropertySourcesList;
import com.ettrema.common.Service;
import com.ettrema.event.EventManager;
import org.spliffy.server.apps.ApplicationManager;
import org.spliffy.server.db.Organisation;
import org.spliffy.server.db.Website;
import org.spliffy.server.db.utils.OrganisationDao;
import org.spliffy.server.db.utils.WebsiteDao;

/**
 *
 * @author brad
 */
public class SpliffyResourceFactory implements ResourceFactory, Service {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SpliffyResourceFactory.class);

    public static RootFolder getRootFolder() {
        if (HttpManager.request() != null) {
            return (RootFolder) HttpManager.request().getAttributes().get("_spliffy_root_folder");
        } else {
            return null;
        }
    }
    private final UserDao userDao;
    private final WebsiteDao websiteDao;
    private final OrganisationDao organisationDao;
    private final SpliffySecurityManager securityManager;
    private final Services services;
    private final ApplicationManager applicationManager;
    private final EventManager eventManager;
    private final PropertySourcesList propertySources;
    private final SessionManager sessionManager;

    public SpliffyResourceFactory(UserDao userDao, SpliffySecurityManager securityManager, Services services, ApplicationManager applicationManager, EventManager eventManager, PropertySourcesList propertySources, SessionManager sessionManager) {
        this.websiteDao = new WebsiteDao();
        this.organisationDao = new OrganisationDao();
        this.userDao = userDao;
        this.securityManager = securityManager;
        this.services = services;
        this.applicationManager = applicationManager;
        this.eventManager = eventManager;
        this.propertySources = propertySources;
        this.sessionManager = sessionManager;
    }

    @Override
    public void start() {
        applicationManager.init(this);
    }

    @Override
    public void stop() {
        applicationManager.shutDown();
    }

    @Override
    public Resource getResource(String host, String sPath) throws NotAuthorizedException, BadRequestException {
        Path path = Path.path(sPath);
        Resource r = find(host, path);
        return r;
    }

    private Resource find(String host, Path p) throws NotAuthorizedException, BadRequestException {
        if (host.contains(":")) {
            host = host.substring(0, host.indexOf(":"));
        }

        if (p.isRoot()) {
            Resource rootFolder = (Resource) HttpManager.request().getAttributes().get("_spliffy_root_folder");
            if (rootFolder == null) {
                Website website = websiteDao.getWebsite(host, SessionManager.session());
                if (website == null) {
                    Organisation org = OrganisationDao.getRootOrg(SessionManager.session());
                    if( org == null ) {
                        throw new RuntimeException("No root organisation");
                    }
                    rootFolder = new OrganisationFolder(services, applicationManager, org);
                } else {
                    rootFolder = new WebsiteRootFolder(services, applicationManager, website);
                }
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

    public SessionManager getSessionManager() {
        return sessionManager;
    }
}
