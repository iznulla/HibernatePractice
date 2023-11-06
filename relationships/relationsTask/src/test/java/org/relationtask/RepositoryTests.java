package org.relationtask;


import lombok.Cleanup;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.relationstask.entity.*;
import org.relationstask.hibernateUtils.ConnectionUtils;

import java.util.List;
import java.util.stream.Stream;

public class RepositoryTests {
    @Test
    public void checkAddCourse() {
        @Cleanup SessionFactory sessionFactory = ConnectionUtils.sessionFactory();
        @Cleanup Session session = sessionFactory.openSession();
        session.beginTransaction();
        Courses courses = Courses.builder().name("Java Enterprise").build();
        session.save(courses);
        session.getTransaction().commit();
    }

    @Test
    public void checkAddStudents() {
        @Cleanup SessionFactory sessionFactory = ConnectionUtils.sessionFactory();
        @Cleanup Session session = sessionFactory.openSession();
        session.beginTransaction();
        Courses courses = session.get(Courses.class, 4L);
        Students students = Students.builder().name("Xzibit").build();
        courses.setStudent(students);
        session.save(courses);
        session.save(students);
        session.getTransaction().commit();
    }

    @Test
    public void checkGetStudFromCourses() {
        @Cleanup SessionFactory sessionFactory = ConnectionUtils.sessionFactory();
        @Cleanup Session session = sessionFactory.openSession();
        session.beginTransaction();
        Courses courses = session.get(Courses.class, 2L);
        List<Students> students;
        students = courses.getStudents();
        System.out.println(students);
        session.getTransaction().commit();
    }

    @Test
    public void checkAddProfile() {
        @Cleanup SessionFactory sessionFactory = ConnectionUtils.sessionFactory();
        @Cleanup Session session = sessionFactory.openSession();
        session.beginTransaction();
        Students students = session.get(Students.class, 9L);
        StudentProfile studentProfile = StudentProfile.builder()
                .grade(6)
                .build();
        studentProfile.setStudent(students);
        session.save(studentProfile);
        session.getTransaction().commit();
    }

    @Test
    public void checkDeleteStudentUnderGrade() {
        @Cleanup SessionFactory sessionFactory = ConnectionUtils.sessionFactory();
        @Cleanup Session session = sessionFactory.openSession();
        session.beginTransaction();
        Courses courses = session.get(Courses.class, 1L);
        List<Students> students = courses.getStudents();
        for (Students sp : students) {
            if (sp.getStudentProfile().getGrade() < 6) {
                session.delete(sp);
//                students.remove(sp);
            }
        }
//        session.refresh(courses);
        session.getTransaction().commit();
    }

    @Test
    public void checkDelCourse() {
        @Cleanup SessionFactory sessionFactory = ConnectionUtils.sessionFactory();
        @Cleanup Session session = sessionFactory.openSession();
        session.beginTransaction();
        List<Courses> courses = session.createQuery("SELECT a FROM Courses a", Courses.class).getResultList();
        Courses course = new Courses();
        for (Courses c : courses) {
            if (c.getName().equals("Java Enterprise")) {
                course = c;
            }
        }
        session.delete(course);
        session.getTransaction().commit();
    }

    @Test
    public void checkGetStud() {
        @Cleanup SessionFactory sessionFactory = ConnectionUtils.sessionFactory();
        @Cleanup Session session = sessionFactory.openSession();
        session.beginTransaction();
        Students students = session.get(Students.class, 6L);
        Courses students1 = session.get(Courses.class, 2L);
        System.out.println(students.getCourses().getName() +" "+ students1.getName());
        session.getTransaction().commit();
    }

    @Test
    public void checkAddTrener() {
        @Cleanup SessionFactory sessionFactory = ConnectionUtils.sessionFactory();
        @Cleanup Session session = sessionFactory.openSession();
        session.beginTransaction();
        Trener trener = Trener.builder()
                .name("Pavel").build();
        session.save(trener);
        session.getTransaction().commit();
    }

    @Test
    public void checkAddTrenerCourse() {
        @Cleanup SessionFactory sessionFactory = ConnectionUtils.sessionFactory();
        @Cleanup Session session = sessionFactory.openSession();
        session.beginTransaction();
        Trener trener = session.get(Trener.class, 3L);
        Courses courses = session.get(Courses.class, 2L);
        TrenerCourses trenerCourses = TrenerCourses.builder().build();
        trenerCourses.setCourse(courses);
        trenerCourses.setTrener(trener);
        session.save(trenerCourses);
        session.getTransaction().commit();
    }

    @Test
    public void checkGetCoursesTreners() {
        @Cleanup SessionFactory sessionFactory = ConnectionUtils.sessionFactory();
        @Cleanup Session session = sessionFactory.openSession();
        session.beginTransaction();
        Trener trener = session.get(Trener.class, 3L);
        Courses courses = session.get(Courses.class, 5L);
        List<TrenerCourses> trenerCourses = trener.getTrenerCourses();
        List<TrenerCourses> trenerList = courses.getTrenerCourses();
        for (TrenerCourses t : trenerCourses) {
            System.out.println(t.getCourse());
        }
        for (TrenerCourses t : trenerList) {
            System.out.println(t.getTrener());
        }
        session.getTransaction().commit();
    }
}
