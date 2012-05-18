package org.spliffy.sync.event;

import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.ettrema.event.Event;
import com.ettrema.event.EventManager;

/**
 *
 * @author brad
 */
public class EventUtils {

    public static void fireQuietly(EventManager eventManager, Event e) {
        try {
            eventManager.fireEvent( e );
        } catch( ConflictException | BadRequestException | NotAuthorizedException ex ) {
            throw new RuntimeException( ex );
        }
    }
}
