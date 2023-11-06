package org.relationstask.entity;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

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
