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
package org.spliffy.server.web.templating;

import com.bradmcevoy.common.Path;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author brad
 */
public class WebResource {
    
    private Map<String,String> atts = new HashMap<>();
    
    private final Path webPath;
    
    private String tag;
    
    private String body;

    public WebResource(Path webPath) {
        this.webPath = webPath;
    }

    
    
    /**
     * Eg <script>, <link>, <meta>
     * 
     * @return 
     */
    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
    
   
    /**
     * Eg src, property, content, type
     * 
     * @return 
     */
    public Map<String, String> getAtts() {
        return atts;
    }

    public void setAtts(Map<String, String> atts) {
        this.atts = atts;
    }

    /**
     * The body of the tag, such as an inline script
     * 
     * @return 
     */
    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }           
    
    public String toHtml(String themeName) {
        StringBuilder sb = new StringBuilder();
        sb.append("<").append(tag).append(" ");
        for( Map.Entry<String, String> entry : atts.entrySet()) {
            String adjustedValue = adjustRelativePath(entry.getKey(), entry.getValue(), themeName);
            sb.append(entry.getKey()).append("=\"").append(adjustedValue).append("\" ");
        }
        if( body != null && body.length()>0 ) {
            sb.append(">").append(body).append("</").append(tag).append(">");
        } else {
            sb.append("/>");
        }
        return sb.toString();
    }

    /**
     * TODO: make use theme, or something...
     * 
     * If the attribute name is src or href, checks the value to see if
     * its relative, and if so return an absolute path, assuming webresource
     * root is /templates
     * 
     * @param value
     * @return 
     */
    private String adjustRelativePath(String name, String value, String themeName) {
        if( name.equals("href") || name.equals("src")) {
            if( value != null && value.length() > 0 ) {
                if( !value.startsWith("/") && !value.startsWith("http")) {
                    return evaluateRelativePath(value, themeName);
                }
            }
        }
        return value;
    }

    private String evaluateRelativePath(String value, String themeName) {
        Path relative = Path.path(value);
        Path p = webPath;
        for( String relPart : relative.getParts()) {
            switch (relPart) {
                case "..":
                    p = p.getParent();
                    break;
                case ".":
                    break;
                default:
                    // we want to transform hard coded theme references to the configured theme
                    // so if we have eg "../themes/yellow/style.css", but configured theme is "blue", then need to transform to -> "/templates/themes/blue/style.css"
                    if( p.getName().equals("themes")) {
                        p = p.child(themeName); 
                    } else {
                        p = p.child(relPart);
                    }
                    break;
            }
        }
        return p.toString();
    }
}
