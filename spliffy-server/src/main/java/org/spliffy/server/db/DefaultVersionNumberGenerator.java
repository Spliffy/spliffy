package org.spliffy.server.db;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Just uses locking. Only useful for single server - will not work in a cluster!
 * 
 * And its syncronized, so will not scale well with a large number of transactions
 * 
 * Just a piece of crap, really...
 *
 * @author brad
 */
public class DefaultVersionNumberGenerator implements VersionNumberGenerator{

    private Map<UUID,Long> mapOfCounters = new HashMap<>();
    
    @Override
    public synchronized long nextVersionNumber(Repository r) {
        Long l = mapOfCounters.get(r.getId());
        if( l == null ) {
            l = 0l;
        } else {
            l++;
        }
        mapOfCounters.put(r.getId(), l);
        return l;
    }

}
