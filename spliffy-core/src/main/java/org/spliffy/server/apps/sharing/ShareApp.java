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
package org.spliffy.server.apps.sharing;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Resource;
import org.spliffy.server.web.templating.MenuItem;
import java.util.List;
import org.spliffy.server.apps.AppConfig;
import org.spliffy.server.apps.Application;
import org.spliffy.server.db.Profile;
import org.spliffy.server.apps.orgs.OrganisationFolder;
import org.spliffy.server.web.RootFolder;
import org.spliffy.server.web.Services;
import org.spliffy.server.web.SpliffyResourceFactory;
import org.spliffy.server.web.sharing.SharesFolder;

/**
 *
 * @author brad
 */
public class ShareApp implements Application{

    private Services services;

    @Override
    public String getInstanceId() {
        return "sharing";
    }
        
    
    
    @Override
    public Resource getPage(Resource parent, String childName) {
        /**
         * This is for locating share invitations, which are outside the scope of any user
         */
        if( parent instanceof OrganisationFolder) {            
            OrganisationFolder rf = (OrganisationFolder) parent;
            if( childName.equals("share")) {
                return new SharesFolder("share", rf);
            }            
        }
        return null;
    }

    @Override
    public void init(SpliffyResourceFactory resourceFactory, AppConfig config) throws Exception {
        this.services = resourceFactory.getServices();
    }

    @Override
    public void shutDown() {
        
    }

    @Override
    public void addBrowseablePages(CollectionResource parent, List<Resource> children) {
        
    }

    @Override
    public void initDefaultProperties(AppConfig config) {
    
    }
    
    @Override
    public void appendMenu(List<MenuItem> list, Resource r, Profile user, RootFolder rootFolder) {
        
    }
    
}
