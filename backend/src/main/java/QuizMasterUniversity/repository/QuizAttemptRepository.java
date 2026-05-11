package QuizMasterUniversity.repository;

import QuizMasterUniversity.dto.QuizResultProjection;
import QuizMasterUniversity.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

    List<QuizAttempt> findByStudentId(Long studentId);

    List<QuizAttempt> findByQuizId(Long quizId);

    List<QuizAttempt> findByQuizIdAndStudentId(Long quizId, Long studentId);

    Optional<QuizAttempt> findByIdAndStudentId(Long id, Long studentId);

    long countByQuizIdAndStudentId(Long quizId, Long studentId);

    @Query(value = "SELECT qa.id AS attemptId, " +
            "u.id AS studentId, " +
            "u.first_name AS studentFirstName, " +
            "u.last_name AS studentLastName, " +
            "u.email AS studentEmail, " +
            "qa.score AS score, " +
            "qa.max_score AS maxScore, " +
            "qa.finished_at AS finishedAt, " +
            "CASE WHEN qa.max_score = 0 THEN 0 ELSE qa.score * 100.0 / qa.max_score END AS percent " +
            "FROM quiz_attempts qa " +
            "JOIN users u ON qa.student_id = u.id " +
            "WHERE qa.quiz_id = :quizId AND qa.status IN ('COMPLETED', 'TIMEOUT') " +
            "ORDER BY percent DESC",
            nativeQuery = true)
    List<QuizResultProjection> findQuizResultsByQuizId(@Param("quizId") Long quizId);

    @Query(value = "SELECT AVG(qa.score) FROM quiz_attempts qa " +
            "WHERE qa.quiz_id = :quizId AND qa.status IN ('COMPLETED', 'TIMEOUT')",
            nativeQuery = true)
    BigDecimal findAverageScoreByQuizId(@Param("quizId") Long quizId);
}