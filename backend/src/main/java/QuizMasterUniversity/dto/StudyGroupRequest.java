package QuizMasterUniversity.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class StudyGroupRequest {
    @NotBlank(message = "Group name is required")
    @Size(max = 30, message = "Group name must be at most 30 characters")
    private String name;

    @Min(value = 1, message = "Course number must be between 1 and 6")
    @Max(value = 6, message = "Course number must be between 1 and 6")
    private Integer courseNumber;

    @Size(max = 120, message = "Speciality must be at most 120 characters")
    private String speciality;
}
