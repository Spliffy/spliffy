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

import com.bradmcevoy.http.Resource;
import com.ettrema.event.EventManager;
import java.util.ArrayList;
import java.util.List;
import org.spliffy.server.apps.calendar.CalendarApp;
import org.spliffy.server.apps.contacts.ContactsApp;
import org.spliffy.server.apps.login.LoginApp;
import org.spliffy.server.apps.sharing.ShareApp;
import org.spliffy.server.apps.signup.SignupApp;
import org.spliffy.server.web.Services;
import org.spliffy.server.web.UserResource;

/**
 *
 * @author brad
 */
public class ApplicationManager {
    private final List<Application> apps;

    public ApplicationManager(List<Application> apps) {
        List<Application> list = new ArrayList<>(apps);
        list.add(new LoginApp());
        list.add(new CalendarApp());
        list.add(new ContactsApp());
        list.add(new ShareApp());
        list.add(new SignupApp());
        this.apps = list;
    }
    
    public void init(Services services, EventManager eventManager) {
        for( Application app : apps ) {
            app.init(services, eventManager);
        }
    }
    
    public void shutDown() {
        for( Application app : apps ) {
            app.shutDown();
        }
    }    
    
    public Resource getNonBrowseablePage(Resource parent, String name) {
        for( Application app : apps ) {
            Resource child = app.getNonBrowseablePage(parent, name);
            if( child != null ) {
                return child;
            }
        }
        return null;
    }

    public void addBrowseablePages(UserResource parent, List<Resource> children) {
        for( Application app : apps ) {
            app.addBrowseablePages(parent, children);
        }
    }
}
