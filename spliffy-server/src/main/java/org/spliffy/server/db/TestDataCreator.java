package org.spliffy.server.db;

import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;
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
                        
            Calendar cal = new Calendar();
            cal.setOwner(t);
            cal.setCreatedDate(new Date());
            cal.setCtag(System.currentTimeMillis());
            cal.setModifiedDate(new Date());
            cal.setName("cal1");
            session.save(cal);
            
//            CalEvent e =new CalEvent();
//            e.setCalendar(cal);
//            e.setCreatedDate(new Date());
//            e.setCtag(System.currentTimeMillis());
//            e.setDescription("Auto generated event");
//            e.setModifiedDate(new Date());
//            e.setName("Auto1");
//            e.setStartDate(new Date());
//            e.setEndDate(new Date( e.getStartDate().getTime() + 1000*60*60*3 )); // 3 hours later            
//            e.setSummary("Some summary goes here");
//            e.setTimezone(TimeZone.getDefault().getID()); // this ruight??
//            session.save(e);
//            
            AddressBook addressBook = new AddressBook();
            addressBook.setName("contacts");
            addressBook.setOwner(t);
            addressBook.setCreatedDate(new Date());
            addressBook.setModifiedDate(new Date());
            addressBook.setDescription("Auto generated");
            session.save(addressBook);
            
            Contact c = new Contact();
            c.setName("contact1");
            c.setAddressBook(addressBook);
            c.setCreatedDate(new Date());
            c.setModifiedDate(new Date());
            c.setGivenName("Joe");
            c.setSurName("Bloggs");
            c.setTelephonenumber("555 1234");
            c.setMail("joe@blogs.com");
            c.setOrganizationName("Bloggs.com");
            c.setUid(UUID.randomUUID().toString());
            session.save(c);
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
