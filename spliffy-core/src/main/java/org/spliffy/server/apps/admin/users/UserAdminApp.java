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
package org.spliffy.server.apps.admin.users;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Resource;
import org.spliffy.server.web.templating.MenuItem;
import java.util.List;
import org.spliffy.server.apps.AppConfig;
import org.spliffy.server.apps.Application;
import org.spliffy.server.db.Profile;
import org.spliffy.server.apps.orgs.OrganisationFolder;
import org.spliffy.server.web.RootFolder;
import org.spliffy.server.web.SpliffyCollectionResource;
import org.spliffy.server.web.SpliffyResourceFactory;

/**
 *
 * @author brad
 */
public class UserAdminApp implements Application {

    private SpliffyResourceFactory resourceFactory;

    @Override
    public String getInstanceId() {
        return "manageUsers";
    }

    @Override
    public void init(SpliffyResourceFactory resourceFactory, AppConfig config) throws Exception {
        this.resourceFactory = resourceFactory;
    }

    @Override
    public Resource getPage(Resource parent, String requestedName) {
        if (parent instanceof OrganisationFolder) {
            if (requestedName.equals("manageUsers")) {
                OrganisationFolder orgFolder = (OrganisationFolder) parent;
                return new UserAdminPage(requestedName,orgFolder.getOrganisation(), (SpliffyCollectionResource) parent, resourceFactory.getServices());
            }
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
    }

    @Override
    public void appendMenu(List<MenuItem> list, Resource r, Profile user, RootFolder rootFolder) {
        if (rootFolder instanceof OrganisationFolder) {
            MenuItem m = new MenuItem();
            m.setText("Manage users");
            m.setHref("/manageUsers/");
            m.setId("userAdmin");
            list.add(m);
        }
    }
}
