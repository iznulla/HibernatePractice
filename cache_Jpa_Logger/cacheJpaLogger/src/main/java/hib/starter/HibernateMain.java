package hib.starter;

import hib.starter.Entities.Company;
import hib.starter.Entities.PersonInfo;
import hib.starter.Entities.Role;
import hib.starter.Entities.User;
import hib.starter.utils.ConnectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.time.LocalDate;

@Slf4j
public class HibernateMain {

    public static void main(String[] args) {
        Company company = Company.builder().name("Yandex").build();
        User user = User.builder().username("ivan1@mail.ru")
                .personInfo(PersonInfo.builder().firstname("Valera").lastname("Gulyamov")
                        .birthDay(LocalDate.of(2002, 01, 01)).build())
                .age(23)
                .role(Role.ADMIN)
                .company(company)
                .build();
//        log.info("User object in transient state: {}", user);
        try(SessionFactory sessionFactory = ConnectionUtils.buildSession();
            Session session = sessionFactory.openSession()) {
            session.beginTransaction();
//            session.saveOrUpdate(company);
//            session.saveOrUpdate(user);
            User user1 = session.get(User.class, 2);
            session.delete(user1);
//            Company company1 = session.get(Company.class, 2);
//            System.out.println(company1);
//            log.warn("User first name is changed to: {}", user.getPersonInfo().getFirstname());
//            log.debug("User: {}, session: {}", user, session);
            session.getTransaction().commit();
        } catch (Exception e) {
            log.error("Exception occurred: ", e);
        }
    }
}