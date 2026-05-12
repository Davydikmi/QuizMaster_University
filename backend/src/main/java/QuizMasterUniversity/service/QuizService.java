package QuizMasterUniversity.service;

import QuizMasterUniversity.dto.QuizAssignmentRequest;
import QuizMasterUniversity.dto.QuizAssignmentResponse;
import QuizMasterUniversity.dto.QuizRequest;
import QuizMasterUniversity.dto.QuizResponse;
import QuizMasterUniversity.entity.Course;
import QuizMasterUniversity.entity.AttemptStatus;
import QuizMasterUniversity.entity.Quiz;
import QuizMasterUniversity.entity.QuizAssignment;
import QuizMasterUniversity.entity.StudyGroup;
import QuizMasterUniversity.entity.User;
import QuizMasterUniversity.repository.AttemptAnswerRepository;
import QuizMasterUniversity.repository.CourseRepository;
import QuizMasterUniversity.repository.QuestionRepository;
import QuizMasterUniversity.repository.QuizAttemptRepository;
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
    private final QuizAttemptRepository quizAttemptRepository;
    private final AttemptAnswerRepository attemptAnswerRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final StudyGroupRepository studyGroupRepository;
    private final QuizAssignmentRepository quizAssignmentRepository;

    public Page<QuizResponse> getQuizzes(Pageable pageable) {
        if (isCurrentUserStudent()) {
            User student = getCurrentStudent();
            if (student.getGroup() == null) {
                return Page.empty(pageable);
            }
            return quizRepository.findDistinctByQuizAssignmentsGroupId(student.getGroup().getId(), pageable).map(this::convertToResponse);
        }
        if (isCurrentUserAdmin()) {
            return quizRepository.findAll(pageable).map(this::convertToResponse);
        }
        String email = getCurrentUserEmail();
        return quizRepository.findByCreatorEmail(email, pageable).map(this::convertToResponse);
    }

    public QuizResponse getQuizById(Long id) {
        if (isCurrentUserStudent()) {
            User student = getCurrentStudent();
            if (student.getGroup() == null || quizAssignmentRepository.findByQuizIdAndGroupId(id, student.getGroup().getId()).isEmpty()) {
                throw new IllegalArgumentException("Quiz not found or access denied");
            }
            return quizRepository.findById(id)
                    .map(this::convertToResponse)
                    .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));
        }
        if (isCurrentUserAdmin()) {
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

    @Transactional
    public void deleteQuiz(Long id) {
        Quiz quiz = findQuizForCurrentUser(id);
        var attempts = quizAttemptRepository.findByQuizId(quiz.getId());
        if (!attempts.isEmpty()) {
            var attemptIds = attempts.stream().map(attempt -> attempt.getId()).toList();
            attemptAnswerRepository.deleteSelectedOptionsByAttemptIds(attemptIds);
            attemptAnswerRepository.deleteByAttemptIds(attemptIds);
            quizAttemptRepository.deleteAll(attempts);
        }
        quizAssignmentRepository.deleteByQuizId(quiz.getId());
        questionRepository.deleteByQuizId(quiz.getId());
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
        QuizAssignment assignment = getRelevantAssignment(quiz);
        User student = isCurrentUserStudent() ? getCurrentStudent() : null;
        long completedAttempts = student == null ? 0 : quizAttemptRepository.findByQuizIdAndStudentId(quiz.getId(), student.getId()).stream()
                .filter(attempt -> attempt.getStatus() == AttemptStatus.COMPLETED || attempt.getStatus() == AttemptStatus.TIMEOUT)
                .count();
        return QuizResponse.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
                .description(quiz.getDescription())
                .courseId(quiz.getCourse().getId())
                .courseName(quiz.getCourse().getName())
                .creatorId(quiz.getCreator().getId())
                .creatorEmail(quiz.getCreator().getEmail())
                .creatorName(quiz.getCreator().getLastName() + " " + quiz.getCreator().getFirstName())
                .hasInProgressAttempt(student != null && hasInProgressAttempt(quiz, student))
                .hasCompletedAttempt(student != null && completedAttempts > 0)
                .questionCount((int) questionRepository.countByQuizId(quiz.getId()))
                .timeLimitMinutes(quiz.getTimeLimitMinutes())
                .maxAttempts(quiz.getMaxAttempts())
                .attemptsRemaining(student == null ? quiz.getMaxAttempts() : Math.max(quiz.getMaxAttempts() - (int) completedAttempts, 0))
                .assignedGroupNames(quizAssignmentRepository.findByQuizId(quiz.getId()).stream()
                        .map(quizAssignment -> quizAssignment.getGroup().getName())
                        .sorted()
                        .toList())
                .availableFrom(assignment != null ? assignment.getAvailableFrom() : null)
                .dueDate(assignment != null ? assignment.getDueDate() : null)
                .createdAt(quiz.getCreatedAt())
                .updatedAt(quiz.getUpdatedAt())
                .build();
    }

    private boolean hasInProgressAttempt(Quiz quiz, User student) {
        return quizAttemptRepository.existsByQuizIdAndStudentIdAndStatus(quiz.getId(), student.getId(), AttemptStatus.IN_PROGRESS);
    }

    private User getCurrentStudent() {
        String email = getCurrentUserEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
    }

    private QuizAssignment getRelevantAssignment(Quiz quiz) {
        if (isCurrentUserStudent()) {
            User student = getCurrentStudent();
            if (student.getGroup() == null) {
                return null;
            }
            return quizAssignmentRepository.findByQuizIdAndGroupId(quiz.getId(), student.getGroup().getId()).orElse(null);
        }
        return quizAssignmentRepository.findByQuizId(quiz.getId()).stream()
                .findFirst()
                .orElse(null);
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
