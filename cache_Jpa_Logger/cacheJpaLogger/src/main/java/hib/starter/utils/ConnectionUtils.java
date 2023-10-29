package hib.starter.utils;

import hib.starter.Entities.User;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.sql.Connection;


public class ConnectionUtils {
    public static SessionFactory buildSession() {
        Configuration configuration = new Configuration().configure();
        configuration.configure();
        configuration.addAnnotatedClass(User.class);
        return configuration.buildSessionFactory();
    }
}
