package QuizMasterUniversity.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnswerDto {

    private Long id;
    private String text;
    private boolean isCorrect;
}