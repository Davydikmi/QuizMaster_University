package QuizMasterUniversity.repository;

import QuizMasterUniversity.entity.AttemptAnswer;
import QuizMasterUniversity.entity.Question;
import QuizMasterUniversity.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttemptAnswerRepository extends JpaRepository<AttemptAnswer, Long> {

    Optional<AttemptAnswer> findByAttemptAndQuestion(QuizAttempt attempt, Question question);

    List<AttemptAnswer> findByAttempt(QuizAttempt attempt);

    @Modifying
    @Query(value = "DELETE FROM answer_selected_options WHERE answer_id IN (SELECT id FROM attempt_answers WHERE attempt_id IN :attemptIds)", nativeQuery = true)
    void deleteSelectedOptionsByAttemptIds(@Param("attemptIds") List<Long> attemptIds);

    @Modifying
    @Query(value = "DELETE FROM attempt_answers WHERE attempt_id IN :attemptIds", nativeQuery = true)
    void deleteByAttemptIds(@Param("attemptIds") List<Long> attemptIds);
}
