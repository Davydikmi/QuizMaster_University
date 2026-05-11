package QuizMasterUniversity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailableAttemptResponse {
    private Long quizId;
    private String quizTitle;
    private LocalDateTime availableFrom;
    private LocalDateTime dueDate;
    private Integer maxAttempts;
    private Integer attemptsRemaining;
}
