package QuizMasterUniversity.repository;

import QuizMasterUniversity.entity.Quiz;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {

    Page<Quiz> findByCreatorEmail(String creatorEmail, Pageable pageable);

    Optional<Quiz> findByIdAndCreatorEmail(Long id, String creatorEmail);

    Page<Quiz> findDistinctByQuizAssignmentsGroupId(Long groupId, Pageable pageable);
}
