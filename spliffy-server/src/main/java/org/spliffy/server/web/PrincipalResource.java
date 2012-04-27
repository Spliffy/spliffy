package org.spliffy.server.web;

import com.ettrema.http.acl.DiscretePrincipal;
import com.ettrema.http.caldav.CalDavPrincipal;
import com.ettrema.http.carddav.CardDavPrincipal;

/**
 * Defines a type of principal which is also a Resource, so it can
 * be returned from the resource factory
 *
 * @author brad
 */
public interface PrincipalResource  extends DiscretePrincipal, CalDavPrincipal, CardDavPrincipal {
    
}
