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
package org.spliffy.server.apps.signup;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Resource;
import com.ettrema.event.EventManager;
import org.spliffy.server.web.templating.MenuItem;
import java.util.List;
import org.spliffy.server.apps.AppConfig;
import org.spliffy.server.apps.Application;
import org.spliffy.server.db.Profile;
import org.spliffy.server.web.RootFolder;
import org.spliffy.server.web.Services;
import org.spliffy.server.web.SpliffyResourceFactory;
import org.spliffy.server.web.WebsiteRootFolder;

/**
 *
 * @author brad
 */
public class SignupApp implements Application {

    private Services services;
    private EventManager eventManager;
    private String signupPageName = "signup";

    public SignupApp() {
    }

    
    
    @Override
    public String getInstanceId() {
        return "signup";
    }

        
    @Override
    public void init(SpliffyResourceFactory resourceFactory) {
        this.services = resourceFactory.getServices();
        this.eventManager = resourceFactory.getEventManager();
    }

    @Override
    public Resource getPage(Resource parent, String requestedName) {
        if (parent instanceof WebsiteRootFolder) {
            WebsiteRootFolder rf = (WebsiteRootFolder) parent;
            if (requestedName.equals(signupPageName)) {
                return new SignupPage(requestedName, rf, services);
            }
        } else {
            System.out.println("not org: " + parent.getClass());
        }
        return null;
    }

    @Override
    public void addBrowseablePages(CollectionResource parent, List<Resource> children) {
    }

    @Override
    public void shutDown() {
    }

    @Override
    public void initDefaultProperties(AppConfig config) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public void appendMenu(List<MenuItem> list, Resource r, Profile user, RootFolder rootFolder) {
        
    }
    
}
