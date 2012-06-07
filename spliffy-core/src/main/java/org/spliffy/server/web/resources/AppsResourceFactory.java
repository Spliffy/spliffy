/*
 * Copyright (C) 2012 McEvoy Software Ltd
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.spliffy.server.web.resources;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import org.spliffy.server.apps.Application;
import org.spliffy.server.apps.ApplicationManager;
import org.spliffy.server.apps.ResourceApplication;
import org.spliffy.server.apps.website.WebsiteRootFolder;

/**
 * Locates resources provided by applications
 * 
 * This can be easier and faster then building a tree structure, especially for
 * static insecure resources such as css files and images
 *
 * @author brad
 */
public class AppsResourceFactory implements ResourceFactory {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AppsResourceFactory.class);
    
    private final ApplicationManager applicationManager;

    public AppsResourceFactory(ApplicationManager applicationManager) {
        this.applicationManager = applicationManager;
    }    
    
    @Override
    public Resource getResource(String host, String sPath) throws NotAuthorizedException, BadRequestException {
        log.info("getResource: " + sPath);
        if (host.contains(":")) {
            host = host.substring(0, host.indexOf(":"));
        }
        Resource rootFolder = applicationManager.getPage(null, host);        
        if (rootFolder instanceof WebsiteRootFolder) {
            WebsiteRootFolder webRoot = (WebsiteRootFolder) rootFolder;
            Path p = Path.path(sPath);
            for( Application app : applicationManager.getApps() ) {
                if( app instanceof ResourceApplication ) {
                    ResourceApplication ra = (ResourceApplication) app;
                    Resource r = ra.getResource(webRoot, sPath);
                    if( r != null ) {
                        return r;
                    }
                }
            }
        }
        return null;
    }
}
