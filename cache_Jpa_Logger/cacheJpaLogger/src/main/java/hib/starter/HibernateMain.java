package hib.starter;

import hib.starter.Entities.PersonInfo;
import hib.starter.Entities.User;
import hib.starter.utils.ConnectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.time.LocalDate;

@Slf4j
public class HibernateMain {

    public static void main(String[] args) {
        User user = User.builder().username("Ivan99@mail.ru")
                .personInfo(PersonInfo.builder().firstname("ivan").lastname("Alekseev")
                        .birthDay(LocalDate.of(2000, 01, 01)).build())
                .age(23)
                .build();
        log.info("User object in transient state: {}", user);
        try(SessionFactory sessionFactory = ConnectionUtils.buildSession();
            Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.save(user);
//            User user1 = session.get(User.class, "Ivan@mail.ru");
//            user1.setPersonInfo(PersonInfo.builder().firstname("Andrey").build());
            log.warn("User first name is changed to: {}", user.getPersonInfo().getFirstname());
            log.debug("User: {}, session: {}", user, session);
            session.getTransaction().commit();
        } catch (Exception e) {
            log.error("Exception occurred: ", e);
        }
    }
}