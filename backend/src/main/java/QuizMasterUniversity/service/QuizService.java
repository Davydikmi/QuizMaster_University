package QuizMasterUniversity.service;

import QuizMasterUniversity.dto.QuizAssignmentRequest;
import QuizMasterUniversity.dto.QuizAssignmentResponse;
import QuizMasterUniversity.dto.QuizRequest;
import QuizMasterUniversity.dto.QuizResponse;
import QuizMasterUniversity.entity.Course;
import QuizMasterUniversity.entity.Quiz;
import QuizMasterUniversity.entity.QuizAssignment;
import QuizMasterUniversity.entity.StudyGroup;
import QuizMasterUniversity.entity.User;
import QuizMasterUniversity.repository.CourseRepository;
import QuizMasterUniversity.repository.QuizAssignmentRepository;
import QuizMasterUniversity.repository.QuizRepository;
import QuizMasterUniversity.repository.StudyGroupRepository;
import QuizMasterUniversity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final StudyGroupRepository studyGroupRepository;
    private final QuizAssignmentRepository quizAssignmentRepository;

    public Page<QuizResponse> getQuizzes(Pageable pageable) {
        if (isCurrentUserAdmin() || isCurrentUserStudent()) {
            return quizRepository.findAll(pageable).map(this::convertToResponse);
        }
        String email = getCurrentUserEmail();
        return quizRepository.findByCreatorEmail(email, pageable).map(this::convertToResponse);
    }

    public QuizResponse getQuizById(Long id) {
        if (isCurrentUserAdmin() || isCurrentUserStudent()) {
            return quizRepository.findById(id)
                    .map(this::convertToResponse)
                    .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));
        }
        String email = getCurrentUserEmail();
        Quiz quiz = quizRepository.findByIdAndCreatorEmail(id, email)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found or access denied"));
        return convertToResponse(quiz);
    }

    public QuizResponse createQuiz(QuizRequest request) {
        User creator = getAuthenticatedUser();
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        if (!course.getTeacher().getEmail().equalsIgnoreCase(creator.getEmail()) && !isCurrentUserAdmin()) {
            throw new IllegalArgumentException("Only the owning teacher can create quizzes for this course");
        }

        Quiz quiz = Quiz.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .creator(creator)
                .course(course)
                .timeLimitMinutes(request.getTimeLimitMinutes())
                .maxAttempts(request.getMaxAttempts())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Quiz savedQuiz = quizRepository.save(quiz);
        return convertToResponse(savedQuiz);
    }

    public QuizResponse updateQuiz(Long id, QuizRequest request) {
        Quiz quiz = findQuizForCurrentUser(id);
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        if (!course.getTeacher().getEmail().equalsIgnoreCase(getCurrentUserEmail()) && !isCurrentUserAdmin()) {
            throw new IllegalArgumentException("Only the owning teacher can assign this course to the quiz");
        }

        quiz.setTitle(request.getTitle());
        quiz.setDescription(request.getDescription());
        quiz.setCourse(course);
        quiz.setTimeLimitMinutes(request.getTimeLimitMinutes());
        quiz.setMaxAttempts(request.getMaxAttempts());
        quiz.setUpdatedAt(LocalDateTime.now());

        return convertToResponse(quizRepository.save(quiz));
    }

    public void deleteQuiz(Long id) {
        Quiz quiz = findQuizForCurrentUser(id);
        quizRepository.delete(quiz);
    }

    @Transactional
    public QuizAssignmentResponse assignQuizToGroup(Long quizId, Long groupId, QuizAssignmentRequest request) {
        Quiz quiz = findQuizForCurrentUser(quizId);
        StudyGroup group = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        if (!request.getDueDate().isAfter(request.getAvailableFrom())) {
            throw new IllegalArgumentException("Due date must be after available from");
        }

        QuizAssignment assignment = QuizAssignment.builder()
                .quiz(quiz)
                .group(group)
                .assignedAt(LocalDateTime.now())
                .availableFrom(request.getAvailableFrom())
                .dueDate(request.getDueDate())
                .build();

        QuizAssignment saved = quizAssignmentRepository.save(assignment);
        return convertToAssignmentResponse(saved);
    }

    private Quiz findQuizForCurrentUser(Long id) {
        if (isCurrentUserAdmin()) {
            return quizRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));
        }
        String email = getCurrentUserEmail();
        return quizRepository.findByIdAndCreatorEmail(id, email)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found or access denied"));
    }

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalStateException("User is not authenticated");
        }
        return authentication.getName();
    }

    private User getAuthenticatedUser() {
        String email = getCurrentUserEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
    }

    private boolean isCurrentUserAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }

    private boolean isCurrentUserStudent() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_STUDENT"));
    }

    private QuizResponse convertToResponse(Quiz quiz) {
        return QuizResponse.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
                .description(quiz.getDescription())
                .courseId(quiz.getCourse().getId())
                .courseName(quiz.getCourse().getName())
                .creatorId(quiz.getCreator().getId())
                .creatorEmail(quiz.getCreator().getEmail())
                .timeLimitMinutes(quiz.getTimeLimitMinutes())
                .maxAttempts(quiz.getMaxAttempts())
                .createdAt(quiz.getCreatedAt())
                .updatedAt(quiz.getUpdatedAt())
                .build();
    }

    private QuizAssignmentResponse convertToAssignmentResponse(QuizAssignment assignment) {
        return QuizAssignmentResponse.builder()
                .id(assignment.getId())
                .quizId(assignment.getQuiz().getId())
                .groupId(assignment.getGroup().getId())
                .groupName(assignment.getGroup().getName())
                .assignedAt(assignment.getAssignedAt())
                .availableFrom(assignment.getAvailableFrom())
                .dueDate(assignment.getDueDate())
                .build();
    }
}
