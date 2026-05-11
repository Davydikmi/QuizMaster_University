package QuizMasterUniversity.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "study_groups")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudyGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String name;

    @Column(nullable = false)
    private Integer courseNumber;

    @Column(length = 120)
    private String speciality;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "group", fetch = FetchType.LAZY)
    private List<User> users;

    @OneToMany(mappedBy = "group", fetch = FetchType.LAZY)
    private List<QuizAssignment> quizAssignments;
}