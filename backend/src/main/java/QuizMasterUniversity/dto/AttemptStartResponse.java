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
public class AttemptStartResponse {
    private Long attemptId;
    private Long quizId;
    private String quizTitle;
    private BigDecimal maxScore;
    private LocalDateTime startedAt;
    private String status;
}
