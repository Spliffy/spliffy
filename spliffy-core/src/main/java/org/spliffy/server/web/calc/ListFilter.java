package org.spliffy.server.web.calc;

import com.bradmcevoy.http.Resource;
import org.spliffy.server.web.ResourceList;
import org.spliffy.server.web.SpliffyResource;

/**
 *
 * @author brad
 */
public class ListFilter implements Accumulator {

    ResourceList dest = new ResourceList();

    @Override
    public void accumulate( SpliffyResource r, Object o ) {
        if( o instanceof Boolean ) {
            Boolean b = (Boolean) o;
            if( b.booleanValue() ) {
                dest.add( r );
            }
        }
    }
}
