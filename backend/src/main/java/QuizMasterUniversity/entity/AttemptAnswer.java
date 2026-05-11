package QuizMasterUniversity.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "attempt_answers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttemptAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false)
    private QuizAttempt attempt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal score;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "answer_selected_options",
        joinColumns = @JoinColumn(name = "answer_id"),
        inverseJoinColumns = @JoinColumn(name = "option_id")
    )
    private List<QuestionOption> selectedOptions;
}