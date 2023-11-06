package org.relationstask.hibernateUtils;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.relationstask.entity.*;

public class ConnectionUtils {
    public static SessionFactory sessionFactory() {
        Configuration configuration = new Configuration();
        configuration.configure();
        configuration.addAnnotatedClass(Courses.class);
        configuration.addAnnotatedClass(Students.class);
        configuration.addAnnotatedClass(StudentProfile.class);
        configuration.addAnnotatedClass(Trener.class);
        configuration.addAnnotatedClass(TrenerCourses.class);
        return configuration.buildSessionFactory();
    }

}
