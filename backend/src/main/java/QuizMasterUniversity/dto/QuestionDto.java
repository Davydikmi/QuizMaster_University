package QuizMasterUniversity.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDto {

    private Long id;
    private String text;
    private String type; // MULTIPLE_CHOICE, etc.
    private BigDecimal points;
    private List<AnswerDto> answers;
}