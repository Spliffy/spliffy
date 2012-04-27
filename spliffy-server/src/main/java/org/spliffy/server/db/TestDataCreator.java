package org.spliffy.server.db;

import java.util.Date;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.spliffy.server.web.PasswordManager;

/**
 *
 * @author brad
 */
public class TestDataCreator {

    private final SessionFactory sessionFactory;
    private final PasswordManager passwordManager;
    private boolean enabled = false;

    public TestDataCreator(SessionFactory sessionFactory, PasswordManager passwordManager) {
        this.sessionFactory = sessionFactory;
        this.passwordManager = passwordManager;
    }

    /**
     * Can be called from spring init-method
     *
     */
    public void initTestData() {
        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        checkCreateUser("user1", "password1",session);
        checkCreateUser("user2", "password1",session);
        checkCreateUser("user3", "password1",session);
        tx.commit();
        session.close();
    }

    private void checkCreateUser(String name,String password, Session session) throws HibernateException {
        User t = (User) session.get(User.class, name);
        if (t == null) {
            t = new User();
            t.setName(name);
            passwordManager.setPassword(t, password);
            t.setCreatedDate(new Date());
            t.setModifiedDate(new Date());
            t.setEmail(name + "@spliffy.org");
            session.save(t);
            System.out.println("created test user");
            
            Repository r1 = new Repository();
            r1.setBaseEntity(t);
            r1.setCreatedDate(new Date());
            r1.setName("repo1");            
            session.save(r1);
                        
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
