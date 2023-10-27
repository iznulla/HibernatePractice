# configuration, annotations, methods

# Конфигурация

Конфигурация `Hibernate` состоит из файла сборки конфигураций и класса `Configuration`.

- `SessionFactory` - класс который вызывается с помощью статического метода из класса Configuration, метод называется `buildSessionFactory()` которая отвечает за сборку конфигурации.
- `Session` - сессия которая запрашивает непосредственно коммуникацию с базой данных. Вызывается она с помощью статического метода класса `SessionFactory`, называется `openSession()` . Session имеет большое количество методов, нижу некоторые основные из них.
    - `beginTransaction()`- Начало всех сессий, ставится в начале.
    - `getTransaction().commit()`- Ставится в конце сессии, по слову `commit()` мы можем понять, что собираемся зафиксировать запрос.
    - `get(Object o, object)`
    - `save(Object o)`
    - `saveOrUpdate(Object o)`
    - `update(Object o)`
    - `delete(Object o)`

## Пример создания конфигурации hibernate тестов

### файл hibernate.cfg.xml, этот файл один из основных процессов который предоставляет всякую конфигурационную информацию для класса конфигураций

```xml
<?xml version='1.0' encoding='utf-8'?>
<!DOCTYPE hibernate-configuration PUBLIC
        "-//Hibernate/Hibernate Configuration DTD//EN"
        "http://hibernate.sourceforge.net/hibernate-configuration-3.0.dtd">

<hibernate-configuration>

    <!-- a SessionFactory instance listed as /jndi/name -->
    <session-factory>

        <!-- properties -->
        <property name="connection.url">jdbc:postgresql://localhost:5432/thack</property>
        <property name="connection.driver_class">org.postgresql.Driver</property>
        <property name="connection.username">codela</property>
        <property name="connection.password">1</property>
        <property name="dialect">org.hibernate.dialect.PostgreSQL10Dialect</property>
        <property name="show_sql">true</property>
        <property name="format_sql">true</property>
<!--        <property name="transaction.factory_class">-->
<!--            org.hibernate.transaction.JTATransactionFactory-->
<!--        </property>-->
<!--        <property name="jta.UserTransaction">java:comp/UserTransaction</property>-->

<!--        &lt;!&ndash; mapping files &ndash;&gt;-->
<!--        <mapping resource="org/hibernate/auction/Item.hbm.xml"/>-->
<!--        <mapping resource="org/hibernate/auction/Bid.hbm.xml"/>-->

<!--        &lt;!&ndash; cache settings &ndash;&gt;-->
<!--        <class-cache class="org.hibernate.auction.Item" usage="read-write"/>-->
<!--        <class-cache class="org.hibernate.auction.Bid" usage="read-only"/>-->
<!--        <collection-cache collection="org.hibernate.auction.Item.bids" usage="read-write"/>-->

    </session-factory>

</hibernate-configuration>
```

### класс HubernateRunner

```java
package com.codela;

import com.codela.Entity.Role;
import com.codela.Entity.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.time.LocalDate;

public class HibernateRunner {

    public static void main(String[] args) {

// класс конфигурации который подтягивает информацию из hibernate.cfg.xml
        Configuration configuration = new Configuration();
        configuration.configure();
        configuration.addAnnotatedClass(User.class);

// сборка конфигурации
        try (SessionFactory sessionFactory = configuration.buildSessionFactory();
// передаем собранные конфигурации в сессию, через которую будем обращаться к БД.
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
```

Класс для тестирования нашего User класса, как именно отрабатывает наш класс для сущности

```java
package com.codela;

import com.codela.Entity.User;
import org.junit.jupiter.api.Test;

import javax.persistence.Column;
import javax.persistence.Table;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

class HibernateRunnerTest {
    @Test
    public void testHibernateApi() throws SQLException, IllegalAccessException {
        User user = User.builder().username("ivan1@name.com")
                .firstname("Ivan")
                .lastname("Ivanov")
                .birthDate(LocalDate.of(2000, 01, 01))
                .age(23).build();

// получаем название таблицы и схемы
        String tableName = Optional.of(user.getClass().getAnnotation(Table.class))
                .map(table -> table.schema() + "." + table.name())
                .orElse(user.getClass().getName());

        Field[] fields = user.getClass().getDeclaredFields();

// получаем поля класса User
        String columnNames = Arrays.stream(fields)
                .map(field -> Optional.ofNullable(field.getAnnotation(Column.class))
                        .map(Column::name)
                        .orElse(field.getName())
                ).collect(Collectors.joining(", "));

// получаем значения полей
        String columnValues = Arrays.stream(fields)
                .map(field -> "?").collect(Collectors.joining(", "));
        Connection connection = DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/thack", "codela", "1");
// имитируем sql команду для занесения информации в базу данных
        String sql = String.format("insert into %s (%s) values (%s)",
						 tableName, columnNames, columnValues);

// передаем команду, чтоб выполнить ее
        PreparedStatement preparedStatement = connection.prepareStatement(sql);

        for(int i = 0; i < fields.length; i++) {
            fields[i].setAccessible(true);

// заполняем запрос данными
            preparedStatement.setObject(i + 1, fields[i].get(user));
        }

// отправляем запрос
        preparedStatement.executeUpdate();
        preparedStatement.close();
        connection.close();
    }

}
```

# Аннотации

- `@Entity` - аннотация ставится над классом объекта которая является сущностью
- `@Table(name = "users", schema = "public")` - ставится после аннотации @Entity, она нужна для того, чтоб обозначить к какой именно таблице относится наша модель
- `@Id` - это обязательная аннотации Hibernate, она нужна для установки первичного ключа модели, которая является сущностью
- `@Column(name = "birth_date")`- необязательная аннотация, которая ставится над полями, у которых отличается название поля от столбца в сущности, которую имитирует наш класс
- `@Enumerated(EnumType.*STRING*)`- аннотация ставится над полем которая является Enumerate типом, обычно ставится для ролей.

Пример SQL команды которая создает сущность пользователя

```sql
create table users
(
	username varchar(128) primary key,
	firstname varchar(128),
	lastname varchar(128),
	birth_date date,
	age int,
	role varchar(32)
);
```

Пример кода, которая представляет сущность юзера в java классе

```java
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity
@Table(name = "users", schema = "public")
public class User {
    @Id
    private String username;
    private String firstname;
    private String lastname;
    @Column(name = "birth_date")
    private LocalDate birthDate;
    private Integer age;
    @Enumerated(EnumType.STRING)
    private Role role;
}

public enum Role {
    ADMIN, USER
}
```