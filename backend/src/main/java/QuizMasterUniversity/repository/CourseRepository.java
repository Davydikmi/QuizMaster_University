package QuizMasterUniversity.repository;

import QuizMasterUniversity.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    Page<Course> findByTeacherEmail(String teacherEmail, Pageable pageable);

    Optional<Course> findByIdAndTeacherEmail(Long id, String teacherEmail);
}
