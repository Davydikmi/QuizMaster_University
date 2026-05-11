package QuizMasterUniversity.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuizDto {

    private Long id;
    private String title;
    private String description;
    private UserDto creator;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<QuestionDto> questions;
}