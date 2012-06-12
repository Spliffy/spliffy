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
package org.spliffy.vfs;

import com.bradmcevoy.common.Path;
import org.junit.*;

/**
 *
 * @author brad
 */
public class ResourceManagerTest {
    
    ResourceManager resourceManager;
    
    @Before
    public void setUp() {
        resourceManager = new ResourceManager();
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testFind() {
        Path p = Path.path("/a/b/c");
        VfsItem item = resourceManager.find(p);
    }
}
