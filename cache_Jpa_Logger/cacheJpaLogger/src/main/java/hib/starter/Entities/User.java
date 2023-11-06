package hib.starter.Entities;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString(exclude = {"company", "profile", "userChat"})
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
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private ProfileSynthetic profile;

    @Builder.Default
    @OneToMany(mappedBy = "user")
    private List<UserChat> userChat = new ArrayList<>();


}
