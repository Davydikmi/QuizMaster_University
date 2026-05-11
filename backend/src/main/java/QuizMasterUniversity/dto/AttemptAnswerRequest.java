package QuizMasterUniversity.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class AttemptAnswerRequest {

    @NotNull(message = "Question id is required")
    private Long questionId;

    @NotNull(message = "Selected option ids are required")
    @Size(min = 1, message = "At least one selected option id is required")
    private List<Long> selectedOptionIds;
}
