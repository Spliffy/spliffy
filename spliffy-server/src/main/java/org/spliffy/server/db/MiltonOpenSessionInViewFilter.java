package org.spliffy.server.db;

import com.bradmcevoy.http.Filter;
import com.bradmcevoy.http.FilterChain;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Response;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

/**
 *
 * @author brad
 */
public class MiltonOpenSessionInViewFilter implements Filter{

    private static ThreadLocal<Session> tlSession = new ThreadLocal<>();
    
    private final SessionFactory sessionFactory;
    
    

    public MiltonOpenSessionInViewFilter(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
    
    public static Session session() {
        return tlSession.get();
    }
        
    @Override
    public void process(FilterChain chain, Request request, Response response) {
        Session session = null;
        try {
            session = SessionFactoryUtils.getSession(sessionFactory, true);
            tlSession.set(session);
            System.out.println("opened session");
            chain.process(request, response);
            System.out.println("closed session");
        } finally {
            if( session != null ) {
                SessionFactoryUtils.closeSession(session);
                tlSession.remove();
            }
        }
        
    }

}
