package QuizMasterUniversity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttemptResponse {
    private Long attemptId;
    private Long quizId;
    private Long studentId;
    private String quizTitle;
    private BigDecimal score;
    private BigDecimal maxScore;
    private String status;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private Integer attemptNumber;
}
