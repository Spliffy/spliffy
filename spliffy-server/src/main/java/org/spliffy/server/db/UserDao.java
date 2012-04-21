package org.spliffy.server.db;

import java.util.Date;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

/**
 *
 * @author brad
 */
public class UserDao {

    private final SessionFactory sessionFactory;

    public UserDao(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        initTestData();
    }

    public User getUser(String name) {
        return  (User) MiltonOpenSessionInViewFilter.session().get(User.class, name);
    }

    private void initTestData() {
        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        User t = (User) session.get(User.class, "test");
        if( t == null ) {
            t = new User();
            t.setName("test");
            t.setPasswordDigest("XXX");
            t.setCreatedDate(new Date());
            t.setModifiedDate(new Date());
            session.save(t);
            tx.commit();
            System.out.println("created test user");
        }
    }
}
