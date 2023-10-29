package hib.starter.Entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Entity
@Table(name = "users", schema = "public")
public class User {
    @Id
    @GeneratedValue(generator = "user_gen", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "user_gen", sequenceName = "users_id_seq", allocationSize = 1)
    private Integer id;

    private String username;
    @Embedded
    private PersonInfo personInfo;
    private Integer age;
}
