package QuizMasterUniversity.repository;

import QuizMasterUniversity.entity.QuizAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizAssignmentRepository extends JpaRepository<QuizAssignment, Long> {

    List<QuizAssignment> findByGroupId(Long groupId);

    Optional<QuizAssignment> findByQuizIdAndGroupId(Long quizId, Long groupId);
}
