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
package org.spliffy.server.apps;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Resource;
import com.ettrema.common.Service;
import com.ettrema.event.EventManager;
import java.util.List;
import org.spliffy.server.web.Services;
import org.spliffy.server.web.SpliffyResourceFactory;

/**
 * Defines an extensibility mechanism for Spliffy.
 * 
 * At its simplest this is a means to locate resources defined by the application
 * which exist
 *
 * @author brad
 */
public interface Application {
    
    /**
     * Called on the application when the app starts
     * 
     * @param services
     * @param eventManager 
     */
    void init(SpliffyResourceFactory resourceFactory);
    
    /**
     * Return a resource for the given parent of the given name if this
     * application defines one, and if it is not returned from addBrowseablePages. Or return null otherwise
     * 
     * Usually an Application will check the type of the parent and only
     * return a resource if the type is something its handling, like a UserResource
     * 
     * @param parent
     * @param requestedName
     * @return 
     */
    Resource getNonBrowseablePage(Resource parent, String requestedName);

    /**
     * Add instances of resources which should be browseable from webdav clients
     * 
     * @param parent
     * @param children 
     */
    void addBrowseablePages(CollectionResource parent, List<Resource> children);
    
    /**
     * Causes the application to release all resources. It should be restartable
     */
    void shutDown();
}
