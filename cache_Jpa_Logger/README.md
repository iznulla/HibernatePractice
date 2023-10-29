# JPA, Кэш, Логирование, PrimaryKey

# JPA - Java Persistence API

Спецификация Java, которая предовставляет набор интерфейсов/аннотаций для возможности сохранять в удобном виде Java объекты в БД и наоборот, извлекать информацию из БД (ORM)

Hibernate -  это одна из самых распространенных JPA реализаций.

# Составные объекты моделей Entity, Встроенные компоненты @Embeddable

Встроенные компоненты дают нам возможность составлять объекты из разных объектов, а так же выносить определенные поля в другие объекты. например 

У нас есть `Entity` модель `User`

```java
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Entity
@Table(name = "users", schema = "public")
public class User {
    @Id
    private String username;
    private String firstname;
    private String lastname;
    @Column(name = "birth_date")
    private LocalDate birthDay;
    private Integer age;
}
```

мы хотим чтоб персональные данные хранить в другом объекте, такой объект будет обеспечен аннотацией `@Embeddable`  то есть встраиваемый, затем в объекте из которого мы вынесли мы создаем поле типа нашего объекта в который мы вынесли информацию

```java
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Entity
@Table(name = "users", schema = "public")
public class User {
    @Id
    private String username;
    @Embedded
    private PersonInfo personInfo;
    private Integer age;
}
// мы вынесли в класс PersonInfo  персональную информацию, и вместо этих полей
// добавили поле типа нашего нового класса

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Embeddable
public class PersonInfo {
    private String firstname;
    private String lastname;
    @Column(name = "birth_date")
    private LocalDate birthDay;
}
```

# Авто-генерация Id

Когда за генерирование id в  нашем проекте отвечает БД (обычно всегда так), нам нужно как то сказать об этом нашему Entity классу и в Hibernate есть такая возможность, это 

@GeneratedValue(strategy = GenerationType.IDENTITY)  - это самый популярный вариант когда БД генерирует id, поле ID в классе должен быть Integer

```java
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Entity
@Table(name = "users", schema = "public")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String username;
    @Embedded
    private PersonInfo personInfo;
    private Integer age;
}
```

```sql
create table users
(
	id SERIAL primary key,
	username varchar(128) unique,
	firstname varchar(128),
	lastname varchar(128),
	birth_date date,
	age int
);
```

GenerationType.SEQUENCE - это еще один метод для генерации id в классе Entity

этот метод используется когда в базе данных генерация id происходит с помощью sequence метода, например

у нас такая таблица

```sql
create table users
(
	id SERIAL primary key,
	username varchar(128) unique,
	firstname varchar(128),
	lastname varchar(128),
	birth_date date,
	age int
);

create sequence users_id_seq_1
	owned by users.id;
```

чтоб такой метод генерации айди использовать в нашем Entity, мы должны написать следующим образом

```java
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Entity
@Table(name = "users", schema = "public")
public class User {
    @Id
// здесь даем название генератору и указываем тип
    @GeneratedValue(generator = "user_gen", strategy = GenerationType.SEQUENCE)
// а здесь вызываем созданный геенератор и указываем название сиквенса которая
// в БД. и размер задаем свой на сколько он будет увеличиваться, по дефолту там 50
    @SequenceGenerator(name = "user_gen", 
				sequenceName = "users_id_seq", allocationSize = 1)
    private Integer id;

    private String username;
    @Embedded
    private PersonInfo personInfo;
    private Integer age;
}
```

# Кэширование в Hibernate

В Hibernate есть кэширование запросов, то есть, методы которые делают запрос с помощью SELECT и получают результат автоматически кэшируют результат запроса, это делается для того, чтоб не отправлять и не ждать ответа из базы данных каждый раз.

пройдемся по методам и поймем какие есть методы обращения к базе, которые из них кэшируются, а так же узнаем о методах, которые дают преимущество таблице, а которые непосредственно классу.

`Session.get(Class clazz, String serializabe)` - получает на вход название класса`(Entity)` и название искомого объекта (информацию)

```java
User user = session.get(User.class, "Ivan");
// Hibernate сделает селект запрос и вернет результат и при это закэширует
// результат запроса.
// то-есть если мы отправим еще раз такой запрос с тем же исомым
User user2 = session.get(User.class, "Ivan");
// Hibernate не станет отправлять запрос так как такой запрос уже был найден
// и вернет нам результат

```

Так же стоит отметить, что если бы мы искали не существующего юзера, то Hibrrnate отправлял бы запрос каждый раз, так как ничего в кэш не добавилось.

Следующее что нужно учитывать, то что когда мы находим объект с помощью `Session.get(User.class, “Ivan”)` наш объект попадает в `персистансы`, то есть он в кэше и если мы выполним следующий код

```java
public class HibernateMain {
    public static void main(String[] args) {
        User user = User.builder().username("Ivan@mail.ru")
                .firstname("ivan")
                .lastname("Alekseev")
                .age(23)
                .build();

        try(SessionFactory sessionFactory = ConnectionUtils.buildSession();
            Session session = sessionFactory.openSession()) {
            session.beginTransaction();
// мы записали в объект юзера
            User user1 = session.get(User.class, "Ivan@mail.ru");
// изменили имя юзера
            user1.setFirstname("Andrey");
            System.out.println(user1);
// и когда мы сделали коммит, hibernate сохранил все измения
// то-есть наша запись в базе данных обновила имя Ivan на Andrey
            session.getTransaction().commit();
        }
    }
}
```

- `Session.flush(Object o)` - этот метод зафиксирует изменения в персистан-контексте, если мы не хотим ждать до коммита, и для того, чтоб мы могли делать с нашим кэшем различные операции.
- `Session.clear()` - очищает кэш, в случае, который мы рассматривали выше, если мы добавляем изменения в наш объект, который нашли с помощью `Session.get()`, записали его в переменную, потом изменили, что то в этом объект но мы не хотим, чтоб изменения записались, то мы можем вызвать метод `clear(),` который очистит весь кэш и ничего в `БД` не поменяется.
- `Session.evict(Object o)` - так же как и `clear()`, только принимает объект который нужно почистить из кеша, то есть удалит этот объект из кэша.

# Жизненный цикл Entity

Существует пулл объектов в кеше `Hibenrate`

 `Persistent` в этот кэш объекты попадают после методов

- `Session.get(clazz, id)`
- `Session.createQuery(hql)`
- `Session.save(entity)`
- `Session.saveOrUpdate(entity)`

`Detached` - туда попадают отфильтрованные с помощью следующих методов все `Persistant`.

- `Session.evict(entity)`
- `Session.clear()`
- `Session.close()`

И после отрабатываются методы которые все зафиксируют

- `Session.saveOrUpdate(entity)`
- `Session.update(entity)`
- `Session.merge(entity)`

`Removed` - туда попадают `Persistan` которые были удалены

- `Session.delete(entity)`

`Session.refresh(entity)` - метод обновления которые обновляет кэш из базы данных например

```java
public class HibernateMain {
    public static void main(String[] args) {
        User user = User.builder().username("Ivan@mail.ru")
                .firstname("ivan")
                .lastname("Alekseev")
                .age(23)
                .build();

        try(SessionFactory sessionFactory = ConnectionUtils.buildSession();
            Session session = sessionFactory.openSession()) {
            session.beginTransaction();
// мы записали в объект юзера
            User user1 = session.get(User.class, "Ivan@mail.ru");
// изменили имя юзера
            user1.setFirstname("Andrey");
						session.refresh(user1)
// и тут мы вызываем метод обновления refresh() который обновит объект информацией
//из БД, то есть там был Ivan он таковым и останется не смотря на то, что мы 
// изменяли имя юзера в коде
            System.out.println(user1);
// и когда мы сделали коммит, hibernate сохранит так же имя Ivan
            session.getTransaction().commit();
        }
    }
}
```

Так же есть метод `Session.merge(entity)`, он работает так же как и дефолтно мы работали с `Session.get` и потом изменили имя объекта, merge сделает то же самое!

Session.isDirty() - проверяет есть ли изменения в наших класса отличные от объектов в БД то-есть если мы поделали так

```java
public class HibernateMain {
    public static void main(String[] args) {
        User user = User.builder().username("Ivan@mail.ru")
                .firstname("ivan")
                .lastname("Alekseev")
                .age(23)
                .build();

        try(SessionFactory sessionFactory = ConnectionUtils.buildSession();
            Session session = sessionFactory.openSession()) {
            session.beginTransaction();
// мы записали в объект юзера
            User user1 = session.get(User.class, "Ivan@mail.ru");
// изменили имя юзера
            user1.setFirstname("Andrey");
// то isDirty() вернет true
						session.isDirty();
            session.getTransaction().commit();
        }
    }
}
```

# Логирование

Логи - могут

- `Выводиться на консоль`
- `Сохранятся в базе данных или в файл`
- `Выводиться на TCP/IP`

Спецификацией для логов является `slf4j api`, наше приложение через этот апи обращается к библиотеке, которая дает инструментарий для работы с логами

Есть разные уровни логирования

**TRACE→DEBUG→INFO→WARN→ERROR→FATAL**

В зависимости от того, какой уровень мы укажем, будут проигнорированы все предыдущие уровни, например если укажем **ERROR**, то буду показаны **FATAL**, а все которые были до ERROR, будут проигнорированы.

Мы будем изучать одну из распространенных библиотек `log4j`, библиотеки типа `loh4j` подключаются с помощью адаптеров, ниже будет файл с зависимостями `maven`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>hib.starter</groupId>
    <artifactId>cacheJpaLogger</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.source>11</maven.compiler.source>
        <maven.compiler.target>11</maven.compiler.target>
    </properties>

    <dependencies>
        <dependency>
<!-- это и адаптер и библиотека -->
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-log4j12</artifactId>
            <version>2.0.7</version>
        </dependency>
        
    </dependencies>

</project>
```

Далее мы должны создать xml файл конфигурации логов

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE log4j:configuration SYSTEM "log4j.dtd">
<log4j:configuration>
<!-- appender - это набор инструкций в каком виде мы хотим получать логи
напрмер первый appender это для вывода логов в консоль
класс тут указан существующий класс в библиотеке log4j -->
    <appender name="console" class="org.apache.log4j.ConsoleAppender">
<!-- тут мы указываем куда направлять информацию -->
        <param name="target" value="System.out"/>
<!-- тут мы выбираем шаблон-паттерн, в каком виде нам видеть информацию -->
        <layout class="org.apache.log4j.PatternLayout">
<!--	%d - дата  %p - уровень логирования, %c - класс в которой произошла ошибка
%L - строка где произошла ошибка %m%n - сообщение об ошибке -->
            <param name="ConversionPattern"
                   value="%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n" />
        </layout>
    </appender>

    <appender name="file" class="org.apache.log4j.RollingFileAppender">

<!-- тут казываем что мы собираемся добавлять в логи, а не переписывать -->
        <param name="append" value="false" />

<!-- тут максимальный размер файла -->
        <param name="maxFileSize" value="1KB" />
<!-- тут сколько файлов собираемся копить перед тем как заменять-->
        <param name="maxBackupIndex" value="10" />
        <!-- For Tomcat -->
<!-- тут название какое хотим указать для файла -->
        <param name="file" value="hibernate-starter.log" />
        <layout class="org.apache.log4j.PatternLayout">
            <param name="ConversionPattern"
                   value="%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n" />
        </layout>
    </appender>
<!-- тут мы добавляем appender - ы которые хотим воспроизводить -->
    <root>
<!-- указываем уровень ERROR или DEBUG, об этом говорили выше -->
        <level value="debug" />
        <appender-ref ref="console" />
        <appender-ref ref="file"/>
    </root>

</log4j:configuration>
```

Далее рассмотрим на примере предыдущих реализаций выше

```java
@Slf4j
public class HibernateMain {
// я оставлю это здесь, так как если проект бе lombok, то придется 
// вот так создавать экземпляр логеров в каждом классе
    private static final Logger log = LoggerFactory.getLogger(HibernateMain.class);

    public static void main(String[] args) {
        User user = User.builder().username("Ivan@mail.ru")
                .firstname("ivan")
                .lastname("Alekseev")
                .age(23)
                .build();
// тут мы выводим лог уровня info
        log.info("User object in transient state: {}", user);
        try(SessionFactory sessionFactory = ConnectionUtils.buildSession();
            Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            User user1 = session.get(User.class, "Ivan@mail.ru");
            user1.setFirstname("Andrey");
// здесь уровень warning
            log.warn("User first name is changed to: {}", user1.getFirstname());
//  тут debug
            log.debug("User: {}, session: {}", user1, session);
            session.getTransaction().commit();
        } catch (Exception e) {
// а вот так мы выводим все логи ошибок из Exception
            log.error("Exception occurred: ", e);
        }
    }
}
```