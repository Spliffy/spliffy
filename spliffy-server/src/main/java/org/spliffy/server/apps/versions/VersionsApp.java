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
package org.spliffy.server.apps.versions;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Resource;
import com.ettrema.event.EventManager;
import java.util.List;
import org.spliffy.server.apps.Application;
import org.spliffy.server.db.BaseEntity;
import org.spliffy.server.web.Services;
import org.spliffy.server.web.UserResource;

/**
 *
 * @author brad
 */
public class VersionsApp implements Application {

    private Services services;
    
    @Override
    public void init(Services services, EventManager eventManager) {
        this.services = services;
    }

    @Override
    public Resource getNonBrowseablePage(Resource parent, String requestedName) {
        return null;
    }

    @Override
    public void shutDown() {
        
    }

    @Override
    public void addBrowseablePages(CollectionResource parent, List<Resource> children) {
        if( parent instanceof UserResource) {
            UserResource ur = (UserResource) parent;
            BaseEntity baseEntity = ur.getOwner();
            VersionsRootFolder f = new VersionsRootFolder(ur, baseEntity, services);
            children.add(f);
        }
    }
    
}
