package hib.starter.Entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

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

//    public void setUser(User user) {
//        this.user = user;
//        user.setProfile(this);
//        id = user.getId();
//    }
}

