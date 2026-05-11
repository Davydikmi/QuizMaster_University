package QuizMasterUniversity.service;

import QuizMasterUniversity.dto.AttemptAnswerRequest;
import QuizMasterUniversity.dto.AttemptResponse;
import QuizMasterUniversity.dto.AttemptStartResponse;
import QuizMasterUniversity.dto.AvailableAttemptResponse;
import QuizMasterUniversity.dto.QuestionOptionResponse;
import QuizMasterUniversity.dto.QuestionResponse;
import QuizMasterUniversity.entity.AttemptAnswer;
import QuizMasterUniversity.entity.Question;
import QuizMasterUniversity.entity.QuestionOption;
import QuizMasterUniversity.entity.Quiz;
import QuizMasterUniversity.entity.QuizAssignment;
import QuizMasterUniversity.entity.QuizAttempt;
import QuizMasterUniversity.entity.AttemptStatus;
import QuizMasterUniversity.entity.User;
import QuizMasterUniversity.entity.UserRole;
import QuizMasterUniversity.repository.AttemptAnswerRepository;
import QuizMasterUniversity.repository.QuestionOptionRepository;
import QuizMasterUniversity.repository.QuestionRepository;
import QuizMasterUniversity.repository.QuizAssignmentRepository;
import QuizMasterUniversity.repository.QuizAttemptRepository;
import QuizMasterUniversity.repository.QuizRepository;
import QuizMasterUniversity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttemptService {

    private final QuizAttemptRepository quizAttemptRepository;
    private final QuizRepository quizRepository;
    private final QuizAssignmentRepository quizAssignmentRepository;
    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final AttemptAnswerRepository attemptAnswerRepository;
    private final UserRepository userRepository;

    public List<AvailableAttemptResponse> getAvailableAttempts() {
        User student = getCurrentStudent();
        var group = student.getGroup();
        if (group == null) {
            return List.of();
        }

        LocalDateTime now = LocalDateTime.now();
        return quizAssignmentRepository.findByGroupId(group.getId()).stream()
                .filter(assignment -> !now.isBefore(assignment.getAvailableFrom()) && !now.isAfter(assignment.getDueDate()))
                .map(assignment -> {
                    int maxAttempts = assignment.getQuiz().getMaxAttempts();
                    long usedAttempts = quizAttemptRepository.findByQuizIdAndStudentId(assignment.getQuiz().getId(), student.getId()).stream()
                            .filter(attempt -> attempt.getStatus() == AttemptStatus.COMPLETED || attempt.getStatus() == AttemptStatus.TIMEOUT)
                            .count();
                    int remaining = Math.max((int) (maxAttempts - usedAttempts), 0);
                    return AvailableAttemptResponse.builder()
                            .quizId(assignment.getQuiz().getId())
                            .quizTitle(assignment.getQuiz().getTitle())
                            .availableFrom(assignment.getAvailableFrom())
                            .dueDate(assignment.getDueDate())
                            .maxAttempts(maxAttempts)
                            .attemptsRemaining(remaining)
                            .build();
                })
                .filter(response -> response.getAttemptsRemaining() > 0)
                .collect(Collectors.toList());
    }

    @Transactional
    public AttemptStartResponse startAttempt(Long quizId) {
        return startAttempt(quizId, false);
    }

    @Transactional
    public AttemptStartResponse startAttempt(Long quizId, boolean retake) {
        User student = getCurrentStudent();
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));

        QuizAssignment assignment = findAssignmentForStudent(quiz, student);
        if (assignment == null) {
            throw new IllegalArgumentException("Quiz is not assigned to your study group");
        }
        LocalDateTime now = LocalDateTime.now();

        // Check if there's already an in-progress attempt and return it
        QuizAttempt existingAttempt = quizAttemptRepository.findByQuizIdAndStudentId(quizId, student.getId()).stream()
                .filter(attempt -> attempt.getStatus() == AttemptStatus.IN_PROGRESS)
                .findFirst()
                .orElse(null);

        if (existingAttempt != null) {
            if (getRemainingSeconds(existingAttempt, assignment, now) <= 0) {
                completeAttempt(existingAttempt, AttemptStatus.TIMEOUT, now);
                return buildAttemptStartResponse(existingAttempt, assignment);
            }
            return buildAttemptStartResponse(existingAttempt, assignment);
        }

        QuizAttempt completedAttempt = quizAttemptRepository
                .findTopByQuizIdAndStudentIdAndStatusInOrderByFinishedAtDesc(
                        quizId,
                        student.getId(),
                        List.of(AttemptStatus.COMPLETED, AttemptStatus.TIMEOUT)
                )
                .orElse(null);
        if (completedAttempt != null && !retake) {
            return buildAttemptStartResponse(completedAttempt, assignment);
        }

        if (assignment != null) {
            if (now.isBefore(assignment.getAvailableFrom())) {
                throw new IllegalArgumentException("Quiz is not yet available");
            }
            if (now.isAfter(assignment.getDueDate())) {
                throw new IllegalArgumentException("Quiz due date has passed");
            }
        }

        long completedAttemptCount = quizAttemptRepository.findByQuizIdAndStudentId(quizId, student.getId()).stream()
                .filter(attempt -> attempt.getStatus() == AttemptStatus.COMPLETED || attempt.getStatus() == AttemptStatus.TIMEOUT)
                .count();
        if (completedAttemptCount >= quiz.getMaxAttempts()) {
            throw new IllegalArgumentException("Maximum number of attempts reached");
        }

        BigDecimal maxScore = calculateMaxScore(quiz);
        QuizAttempt attempt = QuizAttempt.builder()
                .quiz(quiz)
                .student(student)
                .startedAt(now)
                .score(BigDecimal.ZERO)
                .maxScore(maxScore)
                .status(AttemptStatus.IN_PROGRESS)
                .build();

        QuizAttempt savedAttempt = quizAttemptRepository.save(attempt);
        return buildAttemptStartResponse(savedAttempt, assignment);
    }

    private AttemptStartResponse buildAttemptStartResponse(QuizAttempt attempt, QuizAssignment assignment) {
        Quiz quiz = attempt.getQuiz();
        LocalDateTime now = LocalDateTime.now();
        return AttemptStartResponse.builder()
                .attemptId(attempt.getId())
                .quizId(quiz.getId())
                .quizTitle(quiz.getTitle())
                .description(quiz.getDescription())
                .teacherName(quiz.getCreator().getFirstName() + " " + quiz.getCreator().getLastName())
                .courseName(quiz.getCourse().getName())
                .timeLimitMinutes(quiz.getTimeLimitMinutes())
                .questionCount((int) questionRepository.countByQuizId(quiz.getId()))
                .availableFrom(assignment != null ? assignment.getAvailableFrom() : null)
                .dueDate(assignment != null ? assignment.getDueDate() : null)
                .maxScore(attempt.getMaxScore())
                .startedAt(attempt.getStartedAt())
                .finishedAt(attempt.getFinishedAt())
                .remainingSeconds(getRemainingSeconds(attempt, assignment, now))
                .status(attempt.getStatus().name())
                .questions(attempt.getStatus() == AttemptStatus.IN_PROGRESS ? getQuestionResponses(quiz.getId()) : List.of())
                .build();
    }

    @Transactional
    public AttemptResponse saveAnswer(Long attemptId, AttemptAnswerRequest request) {
        QuizAttempt attempt = getAttemptForCurrentStudent(attemptId);
        validateAttemptIsActive(attempt);
        validateAttemptWindow(attempt);

        Question question = questionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));
        if (!question.getQuiz().getId().equals(attempt.getQuiz().getId())) {
            throw new IllegalArgumentException("Question does not belong to this quiz attempt");
        }

        List<QuestionOption> selectedOptions = questionOptionRepository.findAllById(request.getSelectedOptionIds());
        if (selectedOptions.size() != request.getSelectedOptionIds().size()) {
            throw new IllegalArgumentException("One or more selected options are invalid");
        }
        boolean optionBelongsToQuestion = selectedOptions.stream()
                .allMatch(option -> option.getQuestion().getId().equals(question.getId()));
        if (!optionBelongsToQuestion) {
            throw new IllegalArgumentException("Selected options must belong to the target question");
        }

        BigDecimal score = gradeQuestion(question, selectedOptions);
        AttemptAnswer answer = attemptAnswerRepository.findByAttemptAndQuestion(attempt, question)
                .orElse(AttemptAnswer.builder()
                        .attempt(attempt)
                        .question(question)
                        .build());
        answer.setSelectedOptions(selectedOptions);
        answer.setScore(score);
        attemptAnswerRepository.save(answer);

        return mapAttemptResponse(attempt);
    }

    @Transactional
    public AttemptResponse finishAttempt(Long attemptId) {
        QuizAttempt attempt = getAttemptForCurrentStudent(attemptId);
        validateAttemptIsActive(attempt);

        LocalDateTime now = LocalDateTime.now();
        QuizAssignment assignment = findAssignmentForStudent(attempt.getQuiz(), attempt.getStudent());
        AttemptStatus status = getRemainingSeconds(attempt, assignment, now) <= 0 ? AttemptStatus.TIMEOUT : AttemptStatus.COMPLETED;
        completeAttempt(attempt, status, now);

        return mapAttemptResponse(attempt);
    }

    public List<AttemptResponse> getMyResults() {
        User student = getCurrentStudent();
        return quizAttemptRepository.findByStudentId(student.getId()).stream()
                .map(this::mapAttemptResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteAttempt(Long attemptId) {
        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new IllegalArgumentException("Attempt not found"));
        deleteAttempts(List.of(attempt));
    }

    @Transactional
    public void deleteAttemptsForQuiz(Long quizId) {
        deleteAttempts(quizAttemptRepository.findByQuizId(quizId));
    }

    private QuizAttempt getAttemptForCurrentStudent(Long attemptId) {
        User student = getCurrentStudent();
        return quizAttemptRepository.findByIdAndStudentId(attemptId, student.getId())
                .orElseThrow(() -> new IllegalArgumentException("Attempt not found or access denied"));
    }

    private void validateAttemptIsActive(QuizAttempt attempt) {
        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw new IllegalArgumentException("Attempt is not active");
        }
    }

    private void validateAttemptWindow(QuizAttempt attempt) {
        LocalDateTime now = LocalDateTime.now();
        QuizAssignment assignment = findAssignmentForStudent(attempt.getQuiz(), attempt.getStudent());
        if (getRemainingSeconds(attempt, assignment, now) <= 0) {
            throw new IllegalArgumentException("Cannot save answers after due date");
        }
    }

    private QuizAssignment findAssignmentForStudent(Quiz quiz, User student) {
        var group = student.getGroup();
        if (group == null) {
            return null;
        }
        return quizAssignmentRepository.findByQuizIdAndGroupId(quiz.getId(), group.getId())
                .orElse(null);
    }

    private BigDecimal calculateMaxScore(Quiz quiz) {
        return questionRepository.findByQuizId(quiz.getId()).stream()
                .map(Question::getPoints)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Long getRemainingSeconds(QuizAttempt attempt, QuizAssignment assignment, LocalDateTime now) {
        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            return 0L;
        }
        LocalDateTime attemptDeadline = attempt.getStartedAt().plusMinutes(attempt.getQuiz().getTimeLimitMinutes());
        LocalDateTime deadline = assignment != null && assignment.getDueDate().isBefore(attemptDeadline)
                ? assignment.getDueDate()
                : attemptDeadline;
        return Math.max(0L, java.time.Duration.between(now, deadline).getSeconds());
    }

    private void completeAttempt(QuizAttempt attempt, AttemptStatus status, LocalDateTime finishedAt) {
        attempt.setStatus(status);
        attempt.setFinishedAt(finishedAt);
        BigDecimal totalScore = attemptAnswerRepository.findByAttempt(attempt).stream()
                .map(AttemptAnswer::getScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        attempt.setScore(totalScore);
        quizAttemptRepository.save(attempt);
    }

    private void deleteAttempts(List<QuizAttempt> attempts) {
        if (attempts.isEmpty()) {
            return;
        }
        List<Long> ids = attempts.stream().map(QuizAttempt::getId).toList();
        attemptAnswerRepository.deleteSelectedOptionsByAttemptIds(ids);
        attemptAnswerRepository.deleteByAttemptIds(ids);
        quizAttemptRepository.deleteAll(attempts);
    }

    private List<QuestionResponse> getQuestionResponses(Long quizId) {
        return questionRepository.findByQuizId(quizId).stream()
                .map(question -> QuestionResponse.builder()
                        .id(question.getId())
                        .quizId(question.getQuiz().getId())
                        .text(question.getText())
                        .type(question.getType().name())
                        .points(question.getPoints())
                        .orderNum(question.getOrderNum())
                        .options(question.getQuestionOptions().stream()
                                .map(option -> QuestionOptionResponse.builder()
                                        .id(option.getId())
                                        .text(option.getText())
                                        .build())
                                .toList())
                        .build())
                .toList();
    }

    private BigDecimal gradeQuestion(Question question, List<QuestionOption> selectedOptions) {
        if (question.getType() == null) {
            return BigDecimal.ZERO;
        }

        List<QuestionOption> correctOptions = question.getQuestionOptions().stream()
                .filter(QuestionOption::getIsCorrect)
                .collect(Collectors.toList());

        switch (question.getType()) {
            case SINGLE_CHOICE -> {
                if (selectedOptions.size() != 1) {
                    return BigDecimal.ZERO;
                }
                return selectedOptions.get(0).getIsCorrect() ? question.getPoints() : BigDecimal.ZERO;
            }
            case MULTIPLE_CHOICE -> {
                var selectedIds = selectedOptions.stream().map(QuestionOption::getId).collect(Collectors.toSet());
                var correctIds = correctOptions.stream().map(QuestionOption::getId).collect(Collectors.toSet());
                if (selectedIds.equals(correctIds)) {
                    return question.getPoints();
                }
                return BigDecimal.ZERO;
            }
            case TRUE_FALSE -> {
                if (selectedOptions.size() != 1) {
                    return BigDecimal.ZERO;
                }
                return selectedOptions.get(0).getIsCorrect() ? question.getPoints() : BigDecimal.ZERO;
            }
            default -> {
                return BigDecimal.ZERO;
            }
        }
    }

    private AttemptResponse mapAttemptResponse(QuizAttempt attempt) {
        return AttemptResponse.builder()
                .attemptId(attempt.getId())
                .quizId(attempt.getQuiz().getId())
                .studentId(attempt.getStudent().getId())
                .quizTitle(attempt.getQuiz().getTitle())
                .score(attempt.getScore())
                .maxScore(attempt.getMaxScore())
                .status(attempt.getStatus().name())
                .startedAt(attempt.getStartedAt())
                .finishedAt(attempt.getFinishedAt())
                .attemptNumber(computeAttemptNumber(attempt))
                .build();
    }

    private Integer computeAttemptNumber(QuizAttempt attempt) {
        long count = quizAttemptRepository.countByQuizIdAndStudentId(attempt.getQuiz().getId(), attempt.getStudent().getId());
        return (int) count;
    }

    private User getCurrentStudent() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalStateException("User is not authenticated");
        }
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
        if (user.getRole() != UserRole.STUDENT) {
            throw new IllegalArgumentException("Only students can perform quiz attempts");
        }
        return user;
    }
}
