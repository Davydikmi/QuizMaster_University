package QuizMasterUniversity.repository;

import QuizMasterUniversity.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByQuizId(Long quizId);

    Optional<Question> findByIdAndQuizId(Long id, Long quizId);

    long countByQuizId(Long quizId);

    void deleteByQuizId(Long quizId);
}
