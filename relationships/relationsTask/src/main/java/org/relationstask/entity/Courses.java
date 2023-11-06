package org.relationstask.entity;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

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
