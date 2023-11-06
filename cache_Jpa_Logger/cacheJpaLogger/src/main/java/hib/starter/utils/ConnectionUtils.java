package hib.starter.utils;

import hib.starter.Entities.*;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;



public class ConnectionUtils {
    public static SessionFactory buildSession() {
        Configuration configuration = new Configuration().configure();
        configuration.configure();
        configuration.addAnnotatedClass(User.class);
        configuration.addAnnotatedClass(Company.class);
        configuration.addAnnotatedClass(ProfileSynthetic.class);
        configuration.addAnnotatedClass(UserChat.class);
        configuration.addAnnotatedClass(Chat.class);
        return configuration.buildSessionFactory();
    }
}
