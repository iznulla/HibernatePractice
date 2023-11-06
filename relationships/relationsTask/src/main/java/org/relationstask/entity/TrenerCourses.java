package org.relationstask.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

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
