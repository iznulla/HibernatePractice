# Отношение таблиц (MTO, OTM, MTM, OTO)

# ManyToOne

Отношение многие к одному, то-есть когда у нашей сущности есть отношение к таблице у которой много таких же сущностей

Например таблица User имеет отношение ManyToOne к таблице Company, у юзеров одна компания, а у компании много юзеров 

---

```sql
create table users
(
	id BIGSERIAL primary key,
	username varchar(128) unique,
	firstname varchar(128),
	lastname varchar(128),
	birth_date date,
	role varchar(32),
	age int,
	company_id int references company
);

create table company
(
	id serial primary key,
	name varchar(64) not null unique
);
```

---

```java
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString(exclude = "company")
@Data
@EqualsAndHashCode(of = "username")
@Entity
@Table(name = "users", schema = "public")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(unique = true, nullable = false)
    private String username;
    @Embedded
    private PersonInfo personInfo;
    @Enumerated(EnumType.STRING)
    private Role role;
    private Integer age;

    // если мы не хотим в ручную управлять зависимостями то ALL
    // если мы установим optional = false то будет запрос как иннер джоин
    // что бы отработал ленвый запрос, нам нужно чтоб ломбок не вызывал toString у User
    @ManyToOne(cascade = CascadeType.REMOVE, optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;
----------------------------------------------------------------------------------
----------------------------------------------------------------------------------
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@EqualsAndHashCode(of = "name")
@ToString(exclude = "users")
@Entity
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String name;

		@OneToMany(mappedBy = "company")
    private List<User> users;
}
```

`@ManyToOne` - отношение многие к одному, пишется перед полем который мы подключаем к другой сущности(`Entity`). Может принимать несколько параметров

`CascadeType` - это параметр который описывает, что должно происходить с зависимыми объектами, если мы меняем их родительский(главный объект)

- `CascadeType.ALL` - означает, что все действия, которые мы выполняем с родительским объектом, нужно повторить и для его зависимых объектов.
- `CascadeType.PERSIST` - означает, что если мы сохраняем в базу данных родительский объект, то это же нужно сделать и с его зависимыми объектами.
- `CascadeType.REMOVE` - означает, что если мы удаляем в базе родительский объект,  то это же нужно сделать и с его зависимыми объектами.
- `CascadeType.DETACH`  - означает, что если мы удаляем родительский объект из сессии, то это же нужно сделать и с его зависимыми объектами. Пример:

---

`@JoinColumn` - указываем колонку к которой мы делаем объединение.

---

`FetchType.LAZY` - это параметр, который мы устанавливаем в зависимости от того, в какой момент нам нужно вызывать зависимый объект, по дефорлту установлен `FetchType.*EAGER*`

`Установка в LAZY` означает, что когда мы будем делать запрос в `Entity`, запрос не затронет внутренние встроенные типы других таблиц, например, то что мы связали `User` и `Company`, когда мы сделаем запрос в `User`, то запрос в company не произойдет. Если мы используем `Lombok`, то нужно учитывать, что в нем переопределены некоторые методы от `Object`, и даже если у нас стоит параметр `FetchType.LAZY`, запрос все равно произведется и в `User` и в `Company`. Поэтому лучше сделать `@ToString(exclude = "company")` у `User` и переопределить первичный ключ на уникальное поле `@EqualsAndHashCode(of = "username")`

---

# OneToMany

`@OneToMany` - отношение один ко многим, когда одна сущность принимает множество объектов другой сущности с которой она связана. Тот же пример с `User` и `Company`, в этом случаем у нас один юзер работает в одной компании, а в компании работает два и больше юзеров, может принимать параметры

- `mappedBy = “company”` - можем использовать вместо `@JoinColumn` для того, чтоб указать колонку к которой мы привязываемся.
- `orphanRemoval = true` - параметр, который позволяет удалять запись из связанной доченрней таблицы. Следит за изменениями в поле(коллекции и отправляет соответствующие запросы в sql.

> Поле которое мы указываем как OneToMany должна быть коллекцией, так как туда помещаются два и более объектов связанной сущности.
> 

Если мы хотим добавлять и новую компанию и нового юзера в эту компанию мы можем сделать это отдельно добавляя компанию, а затем юзера, либо в классе компании написать следующий метод

```java
@Builder.Default
    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL)
    private Set<User> users = new HashSet<>();

    public void addUser(User user) {
        users.add(user);
        user.setCompany(this);
    }
```

`@Builder.Default` - мы используем для того, чтоб ломбок сгенерил такое поле `private Set<User> users = new HashSet<>();`

---

# OneToOne

`@OneToOne` - отношение один к одному, когда обе сущности связаны по одному, например `User` и `Profile`, где у юзера может быть один профиль, так же и у профиля может быть один юзер.

Существует два вида `id`, синтетический и натуральный

- Натуральный - когда у нас в таблице поле `id` ссылается на уже существующее поле `id` другой таблицы.

```sql
create table users
(
	id BIGSERIAL primary key,
	username varchar(128) unique,
	firstname varchar(128),
	lastname varchar(128),
	birth_date date,
	role varchar(32),
	age int,
	company_id int references company
);

create table company
(
	id serial primary key,
	name varchar(64) not null unique
);

create table profile 
(
	id bigint primary key references users(id),
	street varchar(128),
	language char(2)
);
```

---

```java
public class User {
	@OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
	private Profile profile;
}
__________________________________________________________________________________
----------------------------------------------------------------------------------

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Profile {

    @Id
    @Column(name = "id")
    private Integer id;
    @OneToOne
    @PrimaryKeyJoinColumn
    private User user;
    private String street;
    private String language;

    public void setUser(User user) {
        this.user = user;
        user.setProfile(this);
        id = user.getId();
    }
}
```

Аннотация @OneToOne устанавливается у сущности зависимой таблицы и устанавливаем первичный ключ на таблицу от которой зависит id зависимой таблицы

- Синтетический ключ `id` - это когда в таблица генерирует собственный `id`

```sql
create table profile
(
	id serial primary key,
	user_id bigint not null unique references users (id),
	street varchar(128),
	language char(2)
);
```

---

```java
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Table(name = "profile")
public class ProfileSynthetic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String street;
    private String language;

    public void setUser(User user) {
        this.user = user;
        user.setProfile(this);
    }
}
```

---

# ManyToMany

`@ManyToMany` - это отношение многие ко многим, когда первая сущность имеет связь много второй сущности, а так же вторая сущность имеет связь много к первой сущности например у нас есть сущности `Trainer` и `Course`, один тренер может вести много курсов, а так же `Course` ведут несколько тренеров.

Есть пару вариантов реализации отношения многие ко многим

- `@ManyToMany` - аннотация `hibernate`, через которую можно указать поля, которые будут относиться к тем или иным полям связи
- `@OneToMany - @ManyToOne` (через ассоциативную третью таблицу) - это когда мы можем создать третью таблицу с полями которые связываются с полями сущностей обычно это (`ID`), так же можно указывать дополнительную информацию в этих таблицах

Пример

```sql
create table if not exists courses
(
	id bigserial primary key,
	name varchar(128) unique not null
);
---------------------------------------------------------------------------------
create table if not exists trener
(
	id bigserial primary key,
	name varchar(128)
);
---------------------------------------------------------------------------------
create table if not exists trener_courses
(
	id bigserial primary key,
	course_id bigint references courses(id),
	trener_id bigint references trener(id)
);
```

```java
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@ToString(exclude = "trenerCourses")
@Entity
public class Trener {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Builder.Default
    @OneToMany(mappedBy = "trener")
    private List<TrenerCourses> trenerCourses = new ArrayList<>();
}

---------------------------------------------------------------------------------

@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString(exclude = {"students", "trenerCourses"})
@Builder
@Entity
public class Courses {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Builder.Default
    @OneToMany(mappedBy = "courses", orphanRemoval = true)
    private List<Students> students = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "course")
    private List<TrenerCourses> trenerCourses = new ArrayList<>();

    public void setStudent(Students students) {
        this.students.add(students);
        students.setCourses(this);
    }
}
----------------------------------------------------------------------------------

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity
@Table(name = "trener_courses")
public class TrenerCourses {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "trener_id")
    private Trener trener;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Courses course;

    public void setTrener(Trener trener) {
        this.trener = trener;
        trener.getTrenerCourses().add(this);
    }

    public void setCourse(Courses courses) {
        this.course = courses;
        courses.getTrenerCourses().add(this);
    }
}
```

## Пример тестирования созданных нами отношений

Выведем всех юзеров которые работают в одной и той же компании

```java
class HibernateMainTest {
    @Test
    public void checkOnToMAny() {
   // @Cleanup, аннотация из ломбок, которая автоматически закрывает стимы, 
   // нам не обязательно оборачивать в try чтоб закрыть соеденение с базой данных
       @Cleanup SessionFactory sessionFactory = ConnectionUtils.buildSession();
       @Cleanup Session session = sessionFactory.openSession();
       session.beginTransaction();
       Company company = session.get(Company.class, 2);
       System.out.println(company.getUsers());
       session.getTransaction().commit();
    }

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
    public void checkOrphalRemoval() {
        @Cleanup SessionFactory sessionFactory = ConnectionUtils.buildSession();
        @Cleanup Session session = sessionFactory.openSession();
        session.beginTransaction();
        Company company = session.get(Company.class, 10);
        company.getUsers().removeIf(user -> user.getId().equals(5));
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
```