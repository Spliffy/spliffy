package org.spliffy.server.apps;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author brad
 */
public class AppConfig {

    private Map<String, Object> properties = new HashMap<>();

    public Integer getInt(String name) {
        Object o = properties.get(name);
        if (o == null) {
            return null;
        } else {
            if (o instanceof String) {
                String s = (String) o;
                return Integer.parseInt(s);
            } else if (o instanceof Integer) {
                return (Integer) o;
            } else {
                throw new RuntimeException("Unsupported int type: " + o.getClass());
            }
        }
    }
    
    public void add(String name, Object val) {
        properties.put(name, val);
    }
}
