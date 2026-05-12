package QuizMasterUniversity.service;

import QuizMasterUniversity.dto.QuestionOptionRequest;
import QuizMasterUniversity.dto.QuestionOptionResponse;
import QuizMasterUniversity.dto.QuestionRequest;
import QuizMasterUniversity.dto.QuestionResponse;
import QuizMasterUniversity.entity.Question;
import QuizMasterUniversity.entity.QuestionOption;
import QuizMasterUniversity.entity.QuestionType;
import QuizMasterUniversity.entity.Quiz;
import QuizMasterUniversity.entity.User;
import QuizMasterUniversity.repository.QuestionRepository;
import QuizMasterUniversity.repository.QuizRepository;
import QuizMasterUniversity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final QuizRepository quizRepository;
    private final UserRepository userRepository;

    public List<QuestionResponse> getQuestionsByQuiz(Long quizId) {
        Quiz quiz = findQuizForCurrentUser(quizId);
        return questionRepository.findByQuizId(quiz.getId()).stream()
                .map(this::convertToResponse)
                .toList();
    }

    public QuestionResponse getQuestion(Long quizId, Long questionId) {
        Quiz quiz = findQuizForCurrentUser(quizId);
        Question question = questionRepository.findByIdAndQuizId(questionId, quiz.getId())
                .orElseThrow(() -> new IllegalArgumentException("Question not found for quiz"));
        return convertToResponse(question);
    }

    @Transactional
    public QuestionResponse createQuestion(Long quizId, QuestionRequest request) {
        Quiz quiz = findQuizForCurrentUser(quizId);
        Question question = Question.builder()
                .quiz(quiz)
                .text(request.getText())
                .type(QuestionType.valueOf(request.getType()))
                .points(request.getPoints())
                .orderNum(request.getOrderNum())
                .build();

        List<QuestionOption> optionEntities = buildOptions(request.getOptions(), question);
        question.setQuestionOptions(optionEntities);

        Question saved = questionRepository.save(question);
        return convertToResponse(saved);
    }

    @Transactional
    public QuestionResponse updateQuestion(Long quizId, Long questionId, QuestionRequest request) {
        Quiz quiz = findQuizForCurrentUser(quizId);
        Question question = questionRepository.findByIdAndQuizId(questionId, quiz.getId())
                .orElseThrow(() -> new IllegalArgumentException("Question not found for quiz"));

        question.setText(request.getText());
        question.setType(QuestionType.valueOf(request.getType()));
        question.setPoints(request.getPoints());
        question.setOrderNum(request.getOrderNum());

        List<QuestionOption> optionEntities = buildOptions(request.getOptions(), question);
        question.getQuestionOptions().clear();
        question.getQuestionOptions().addAll(optionEntities);

        Question saved = questionRepository.save(question);
        return convertToResponse(saved);
    }

    public void deleteQuestion(Long quizId, Long questionId) {
        Quiz quiz = findQuizForCurrentUser(quizId);
        Question question = questionRepository.findByIdAndQuizId(questionId, quiz.getId())
                .orElseThrow(() -> new IllegalArgumentException("Question not found for quiz"));
        questionRepository.delete(question);
    }

    private List<QuestionOption> buildOptions(List<QuestionOptionRequest> options, Question question) {
        List<QuestionOption> entities = new ArrayList<>();
        for (QuestionOptionRequest option : options) {
            QuestionOption entity = QuestionOption.builder()
                    .question(question)
                    .text(option.getText())
                    .isCorrect(option.getIsCorrect())
                    .build();
            entities.add(entity);
        }
        return entities;
    }

    private Quiz findQuizForCurrentUser(Long quizId) {
        if (isCurrentUserAdmin()) {
            return quizRepository.findById(quizId)
                    .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));
        }
        if (isCurrentUserStudent()) {
            return quizRepository.findById(quizId)
                    .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));
        }
        String email = getCurrentUserEmail();
        return quizRepository.findByIdAndCreatorEmail(quizId, email)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found or access denied"));
    }

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalStateException("User is not authenticated");
        }
        return authentication.getName();
    }

    private boolean isCurrentUserAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }

    private QuestionResponse convertToResponse(Question question) {
        return QuestionResponse.builder()
                .id(question.getId())
                .quizId(question.getQuiz().getId())
                .text(question.getText())
                .type(question.getType().name())
                .points(question.getPoints())
                .orderNum(question.getOrderNum())
                .options(question.getQuestionOptions().stream()
                        .map(this::convertOptionResponse)
                        .toList())
                .build();
    }

    private QuestionOptionResponse convertOptionResponse(QuestionOption option) {
        QuestionOptionResponse.QuestionOptionResponseBuilder builder = QuestionOptionResponse.builder()
                .id(option.getId())
                .text(option.getText());
        if (!isCurrentUserStudent()) {
            builder.isCorrect(option.getIsCorrect());
        }
        return builder.build();
    }

    private boolean isCurrentUserStudent() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_STUDENT"));
    }
}
