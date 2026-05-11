package QuizMasterUniversity.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class QuizRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must be at most 200 characters")
    private String title;

    @Size(max = 2000, message = "Description must be at most 2000 characters")
    private String description;

    @NotNull(message = "Course id is required")
    private Long courseId;

    @NotNull(message = "Time limit is required")
    @Min(value = 1, message = "Time limit must be at least 1 minute")
    private Integer timeLimitMinutes;

    @NotNull(message = "Max attempts is required")
    @Min(value = 1, message = "Max attempts must be at least 1")
    private Integer maxAttempts;
}
