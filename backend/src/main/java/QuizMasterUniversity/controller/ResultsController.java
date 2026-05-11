package QuizMasterUniversity.controller;

import QuizMasterUniversity.dto.QuizResultsResponse;
import QuizMasterUniversity.service.ResultsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/results")
@RequiredArgsConstructor
public class ResultsController {

    private final ResultsService resultsService;

    @GetMapping("/quiz/{quizId}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<QuizResultsResponse> getQuizResults(@PathVariable Long quizId) {
        return ResponseEntity.ok(resultsService.getQuizResults(quizId));
    }
}
