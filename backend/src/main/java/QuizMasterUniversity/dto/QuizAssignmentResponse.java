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
public class QuizAssignmentResponse {
    private Long id;
    private Long quizId;
    private Long groupId;
    private String groupName;
    private LocalDateTime assignedAt;
    private LocalDateTime availableFrom;
    private LocalDateTime dueDate;
}
