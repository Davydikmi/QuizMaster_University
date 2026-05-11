package QuizMasterUniversity.controller;

import QuizMasterUniversity.dto.AttemptAnswerRequest;
import QuizMasterUniversity.dto.AttemptResponse;
import QuizMasterUniversity.dto.AttemptStartResponse;
import QuizMasterUniversity.dto.AvailableAttemptResponse;
import QuizMasterUniversity.service.AttemptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attempts")
@RequiredArgsConstructor
public class AttemptController {

    private final AttemptService attemptService;

    @GetMapping("/available")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<AvailableAttemptResponse>> getAvailableAttempts() {
        return ResponseEntity.ok(attemptService.getAvailableAttempts());
    }

    @PostMapping("/start/{quizId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<AttemptStartResponse> startAttempt(
            @PathVariable Long quizId,
            @RequestParam(defaultValue = "false") boolean retake
    ) {
        return ResponseEntity.ok(attemptService.startAttempt(quizId, retake));
    }

    @PostMapping("/{attemptId}/answer")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<AttemptResponse> saveAnswer(
            @PathVariable Long attemptId,
            @Valid @RequestBody AttemptAnswerRequest request
    ) {
        return ResponseEntity.ok(attemptService.saveAnswer(attemptId, request));
    }

    @PostMapping("/{attemptId}/finish")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<AttemptResponse> finishAttempt(@PathVariable Long attemptId) {
        return ResponseEntity.ok(attemptService.finishAttempt(attemptId));
    }

    @GetMapping("/my-results")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<AttemptResponse>> getMyResults() {
        return ResponseEntity.ok(attemptService.getMyResults());
    }

    @DeleteMapping("/{attemptId}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<Void> deleteAttempt(@PathVariable Long attemptId) {
        attemptService.deleteAttempt(attemptId);
        return ResponseEntity.noContent().build();
    }
}
