package org.spliffy.server.db.utils;

import java.util.Date;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.spliffy.server.db.User;
import org.spliffy.server.web.PasswordManager;

/**
 *
 * @author brad
 */
public class UserDao {

    private final SessionFactory sessionFactory;
   

    public UserDao(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public User getUser(String name) {
        return  (User) SessionManager.session().get(User.class, name);
    }    
}
