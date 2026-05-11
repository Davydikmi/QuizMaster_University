package QuizMasterUniversity.service;

import QuizMasterUniversity.dto.QuizResultProjection;
import QuizMasterUniversity.dto.QuizResultRowDto;
import QuizMasterUniversity.dto.QuizResultsResponse;
import QuizMasterUniversity.entity.Quiz;
import QuizMasterUniversity.entity.User;
import QuizMasterUniversity.repository.QuizAttemptRepository;
import QuizMasterUniversity.repository.QuizRepository;
import QuizMasterUniversity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResultsService {

    private final QuizAttemptRepository quizAttemptRepository;
    private final QuizRepository quizRepository;
    private final UserRepository userRepository;

    public QuizResultsResponse getQuizResults(Long quizId) {
        Quiz quiz = findQuizForCurrentUser(quizId);
        List<QuizResultProjection> projections = quizAttemptRepository.findQuizResultsByQuizId(quizId);
        List<QuizResultRowDto> rows = projections.stream()
                .map(this::mapProjection)
                .collect(Collectors.toList());

        BigDecimal averageScore = quizAttemptRepository.findAverageScoreByQuizId(quizId);
        if (averageScore == null) {
            averageScore = BigDecimal.ZERO;
        }

        return QuizResultsResponse.builder()
                .results(rows)
                .averageScore(averageScore)
                .build();
    }

    private QuizResultRowDto mapProjection(QuizResultProjection projection) {
        String studentName = projection.getStudentFirstName() + " " + projection.getStudentLastName();
        return QuizResultRowDto.builder()
                .attemptId(projection.getAttemptId())
                .studentId(projection.getStudentId())
                .studentName(studentName)
                .studentEmail(projection.getStudentEmail())
                .score(projection.getScore())
                .maxScore(projection.getMaxScore())
                .percent(projection.getPercent())
                .finishedAt(projection.getFinishedAt())
                .build();
    }

    private Quiz findQuizForCurrentUser(Long quizId) {
        if (isCurrentUserAdmin()) {
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
}
