package QuizMasterUniversity.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "quiz_attempts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    @Column
    private LocalDateTime finishedAt;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal score;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal maxScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttemptStatus status;

    @OneToMany(mappedBy = "attempt", fetch = FetchType.LAZY)
    private List<AttemptAnswer> attemptAnswers;
}