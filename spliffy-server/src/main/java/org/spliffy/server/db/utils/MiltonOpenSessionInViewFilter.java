package org.spliffy.server.db.utils;

import com.bradmcevoy.http.Filter;
import com.bradmcevoy.http.FilterChain;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Response;


/**
 *
 * @author brad
 */
public class MiltonOpenSessionInViewFilter implements Filter {

    private final SessionManager sessionManager;

    public MiltonOpenSessionInViewFilter(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public void process(FilterChain chain, Request request, Response response) {
        try {
            sessionManager.open();
            chain.process(request, response);
        } finally {
            sessionManager.close();
        }

    }
}
