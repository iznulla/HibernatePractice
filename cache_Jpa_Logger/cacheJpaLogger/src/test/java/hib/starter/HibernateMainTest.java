package hib.starter;

import hib.starter.Entities.*;
import hib.starter.utils.ConnectionUtils;
import lombok.Cleanup;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;


class HibernateMainTest {
    @Test
    public void addNewUserAndCompany() {
        @Cleanup SessionFactory sessionFactory = ConnectionUtils.buildSession();
        @Cleanup Session session = sessionFactory.openSession();
        session.beginTransaction();
        User user = User.builder().username("vano@rano.ru").build();
        Company company = Company.builder().name("Google").build();
        company.addUser(user);
        session.save(company);
        session.getTransaction().commit();
    }

    @Test
    public void checkOnToMAny() {
        // @Cleanup, аннотация из ломбок, которая автоматически закрывает стимы, нам не обязательно
        // оборачивать в try чтоб закрыть соеденение с базой данных
       @Cleanup SessionFactory sessionFactory = ConnectionUtils.buildSession();
       @Cleanup Session session = sessionFactory.openSession();
       session.beginTransaction();
       Company company = session.get(Company.class, 2);
       System.out.println(company.getUsers());
       session.getTransaction().commit();
    }

    @Test
    public void checkOrphalRemoval() {
        @Cleanup SessionFactory sessionFactory = ConnectionUtils.buildSession();
        @Cleanup Session session = sessionFactory.openSession();
        session.beginTransaction();
        Company company = session.get(Company.class, 10);
        company.getUsers().removeIf(user -> user.getId().equals(5));
        session.getTransaction().commit();
    }

    @Test
    public void checkOneToOne() {
        @Cleanup SessionFactory sessionFactory = ConnectionUtils.buildSession();
        @Cleanup Session session = sessionFactory.openSession();
        session.beginTransaction();
        User user = User.builder().username("franki@bang.bang").build();
        Profile profile = Profile.builder().language("Ru").street("Moscow").build();
        session.save(user);
        profile.setUser(user);
        session.save(profile);
        session.getTransaction().commit();
    }

    @Test
    public void checkOneToOneSint() {
        @Cleanup SessionFactory sessionFactory = ConnectionUtils.buildSession();
        @Cleanup Session session = sessionFactory.openSession();
        session.beginTransaction();
        User user = User.builder().username("franki@bang.bang").build();
        ProfileSynthetic profile = ProfileSynthetic.builder().language("Ru").street("Moscow").build();
        session.save(user);
        profile.setUser(user);
        session.save(profile);
        session.getTransaction().commit();
    }

    @Test
    public void checkAddNewChat() {
        Chat chat = Chat.builder().name("java").build();
        @Cleanup SessionFactory sessionFactory = ConnectionUtils.buildSession();
        @Cleanup Session session = sessionFactory.openSession();
        session.beginTransaction();
        session.save(chat);
        session.getTransaction().commit();
    }

    @Test
    public void checkAddUserChat() {
        @Cleanup SessionFactory sessionFactory = ConnectionUtils.buildSession();
        @Cleanup Session session = sessionFactory.openSession();
        session.beginTransaction();
        Chat chat = session.get(Chat.class, 1);
        User user = session.get(User.class, 3);
        UserChat userChat = UserChat.builder()
                .createdAt(Instant.now())
                .createdBy(user.getUsername())
                .build();
        userChat.setUser(user);
        userChat.setChat(chat);
        session.save(userChat);
        session.getTransaction().commit();
    }

}