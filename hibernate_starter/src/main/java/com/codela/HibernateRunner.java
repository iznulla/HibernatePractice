package com.codela;


import com.codela.Entity.Role;
import com.codela.Entity.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.time.LocalDate;

public class HibernateRunner {

    public static void main(String[] args) {
        Configuration configuration = new Configuration();
        configuration.configure();
        configuration.addAnnotatedClass(User.class);
        try (SessionFactory sessionFactory = configuration.buildSessionFactory();
        Session session = sessionFactory.openSession()) {
            session.beginTransaction();

            User user = User.builder().username("ivan1@name.com")
                    .firstname("Ivana")
                    .lastname("Ivanov")
                    .birthDate(LocalDate.of(2000, 01, 01))
                    .age(23)
                    .role(Role.ADMIN).build();

            session.save(user);
//            session.saveOrUpdate(user);
//            session.delete(user);
            User user1 = session.get(User.class, "ivan1@name.com");
            System.out.println(user1);
            session.getTransaction().commit();

        }
    }
}
