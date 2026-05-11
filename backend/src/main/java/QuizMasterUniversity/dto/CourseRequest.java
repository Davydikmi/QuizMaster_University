package QuizMasterUniversity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CourseRequest {

    @NotBlank(message = "Course name is required")
    @Size(max = 150, message = "Course name must be at most 150 characters")
    private String name;

    @Size(max = 2000, message = "Description must be at most 2000 characters")
    private String description;
}
