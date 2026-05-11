package QuizMasterUniversity.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface QuizResultProjection {

    Long getAttemptId();

    Long getStudentId();

    String getStudentFirstName();

    String getStudentLastName();

    String getStudentEmail();

    BigDecimal getScore();

    BigDecimal getMaxScore();

    BigDecimal getPercent();

    LocalDateTime getFinishedAt();
}
