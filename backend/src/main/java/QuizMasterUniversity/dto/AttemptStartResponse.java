package QuizMasterUniversity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttemptStartResponse {
    private Long attemptId;
    private Long quizId;
    private String quizTitle;
    private String description;
    private String teacherName;
    private String courseName;
    private Integer timeLimitMinutes;
    private Integer questionCount;
    private LocalDateTime availableFrom;
    private LocalDateTime dueDate;
    private BigDecimal maxScore;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private Long remainingSeconds;
    private String status;
    private List<QuestionResponse> questions;
}
