package QuizMasterUniversity.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizResultDto {

    private Long id;
    private UserDto user;
    private QuizDto quiz;
    private Integer score;
    private Integer totalQuestions;
    private LocalDateTime completedAt;
}