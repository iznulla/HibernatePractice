package hib.starter.Entities;

import lombok.*;

import javax.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @Builder.Default
    @OneToMany(mappedBy = "company", orphanRemoval = true)
    private Set<User> users = new HashSet<>();



    public void addUser(User user) {
        users.add(user);
        user.setCompany(this);
    }

}
