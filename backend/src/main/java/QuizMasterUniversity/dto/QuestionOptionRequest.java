package QuizMasterUniversity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QuestionOptionRequest {

    @NotBlank(message = "Option text is required")
    private String text;

    @NotNull(message = "Option correctness is required")
    private Boolean isCorrect;
}
