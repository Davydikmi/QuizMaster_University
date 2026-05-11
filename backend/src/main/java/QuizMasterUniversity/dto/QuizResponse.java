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
public class QuizResponse {
    private Long id;
    private String title;
    private String description;
    private Long courseId;
    private String courseName;
    private Long creatorId;
    private String creatorEmail;
    private String creatorName;
    private Boolean hasInProgressAttempt;
    private Boolean hasCompletedAttempt;
    private Integer questionCount;
    private Integer timeLimitMinutes;
    private Integer maxAttempts;
    private Integer attemptsRemaining;
    private LocalDateTime availableFrom;
    private LocalDateTime dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
