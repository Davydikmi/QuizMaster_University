package QuizMasterUniversity.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import jakarta.validation.constraints.AssertTrue;
import lombok.Data;

@Data
public class QuizAssignmentRequest {

    @NotNull(message = "Available from is required")
    private LocalDateTime availableFrom;

    @NotNull(message = "Due date is required")
    private LocalDateTime dueDate;

    @AssertTrue(message = "Due date must be after available from")
    private boolean isValidDateRange() {
        if (availableFrom == null || dueDate == null) {
            return true;
        }
        return dueDate.isAfter(availableFrom);
    }
}
