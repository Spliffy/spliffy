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
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.spliffy.server.apps.calendar.CalendarApp;
import org.spliffy.server.apps.contacts.ContactsApp;
import org.spliffy.server.apps.login.LoginApp;
import org.spliffy.server.apps.sharing.ShareApp;
import org.spliffy.server.apps.signup.SignupApp;
import org.spliffy.server.web.SpliffyResourceFactory;
import org.spliffy.server.web.UserResource;

/**
 *
 * @author brad
 */
public class ApplicationManager {
    
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ApplicationManager.class);
    
    private final List<Application> apps;
    private File appsConfigDir;

    public ApplicationManager(List<Application> apps) {
        List<Application> list = new ArrayList<>(apps);
        list.add(new LoginApp());
        list.add(new CalendarApp());
        list.add(new ContactsApp());
        list.add(new ShareApp());
        list.add(new SignupApp());
        this.apps = list;
    }
    
    public void init(SpliffyResourceFactory resourceFactory) {
        if( appsConfigDir == null ) {
            throw new RuntimeException("Please configure an apps config directory in property: appsConfigDir on bean: " + this.getClass());
        }
        if( !appsConfigDir.exists()) {
            if( !appsConfigDir.mkdirs() ) {
                throw new RuntimeException("Apps config folder does not exist and could not be created: " + appsConfigDir.getAbsolutePath());
            }
        }
        for( Application app : apps ) {
            try {
                app.init(resourceFactory);
            } catch (Exception ex) {
                log.error("Application: " + app.getInstanceId() + " failed to start", ex);
            }
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

    public File getAppsConfigDir() {
        return appsConfigDir;
    }

    public void setAppsConfigDir(File appsConfigDir) {
        this.appsConfigDir = appsConfigDir;
    }
        

    /**
     * TODO: make this read from per-app properties
     * 
     * @param app
     * @return 
     */
    public AppConfig getAppConfig(Application app) throws IOException {
        File configFile = new File(appsConfigDir, app.getInstanceId() + ".properties");
        Properties props = new Properties();
        if( configFile.exists()) {
            try( InputStream fin = new FileInputStream(configFile)) {                
                props.load(fin);
            }
            return new AppConfig(props);
        } else { 
            AppConfig config = new AppConfig(props);
            app.initDefaultProperties(config);            
            try( FileOutputStream fout = new FileOutputStream(configFile)) {
                props.store(fout, "auto-generated defaults");
            }
            return config;
        }
    }
}
