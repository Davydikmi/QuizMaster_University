package QuizMasterUniversity.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class QuestionRequest {

    @NotBlank(message = "Question text is required")
    private String text;

    @NotBlank(message = "Question type is required")
    @Pattern(regexp = "SINGLE_CHOICE|MULTIPLE_CHOICE|TRUE_FALSE",
            message = "Question type must be SINGLE_CHOICE, MULTIPLE_CHOICE, or TRUE_FALSE")
    private String type;

    @NotNull(message = "Points are required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Points must be greater than 0")
    private BigDecimal points;

    @NotNull(message = "Order number is required")
    @Min(value = 1, message = "Order number must be at least 1")
    private Integer orderNum;

    @NotEmpty(message = "Question options are required")
    @Valid
    private List<QuestionOptionRequest> options;

    @AssertTrue(message = "SINGLE_CHOICE questions must have exactly one correct option")
    private boolean isSingleChoiceValid() {
        if (!"SINGLE_CHOICE".equals(type) || options == null) {
            return true;
        }
        long correctCount = options.stream()
                .filter(option -> Boolean.TRUE.equals(option.getIsCorrect()))
                .count();
        return correctCount == 1;
    }

    @AssertTrue(message = "TRUE_FALSE questions must have exactly two options and exactly one correct option")
    private boolean isTrueFalseValid() {
        if (!"TRUE_FALSE".equals(type) || options == null) {
            return true;
        }
        if (options.size() != 2) {
            return false;
        }
        long correctCount = options.stream()
                .filter(option -> Boolean.TRUE.equals(option.getIsCorrect()))
                .count();
        return correctCount == 1;
    }

    @AssertTrue(message = "MULTIPLE_CHOICE questions must contain at least one correct option")
    private boolean isMultipleChoiceValid() {
        if (!"MULTIPLE_CHOICE".equals(type) || options == null) {
            return true;
        }
        long correctCount = options.stream()
                .filter(option -> Boolean.TRUE.equals(option.getIsCorrect()))
                .count();
        return correctCount >= 1;
    }
}
