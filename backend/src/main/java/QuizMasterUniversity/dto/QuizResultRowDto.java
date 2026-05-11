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
public class QuizResultRowDto {
    private Long attemptId;
    private Long studentId;
    private String studentName;
    private String studentEmail;
    private BigDecimal score;
    private BigDecimal maxScore;
    private BigDecimal percent;
    private LocalDateTime finishedAt;
}
