package QuizMasterUniversity.controller;

import QuizMasterUniversity.dto.QuizAssignmentRequest;
import QuizMasterUniversity.dto.QuizAssignmentResponse;
import QuizMasterUniversity.dto.QuizRequest;
import QuizMasterUniversity.dto.QuizResponse;
import QuizMasterUniversity.service.QuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    public ResponseEntity<Page<QuizResponse>> getQuizzes(Pageable pageable) {
        return ResponseEntity.ok(quizService.getQuizzes(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    public ResponseEntity<QuizResponse> getQuizById(@PathVariable Long id) {
        return ResponseEntity.ok(quizService.getQuizById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<QuizResponse> createQuiz(@Valid @RequestBody QuizRequest request) {
        return ResponseEntity.ok(quizService.createQuiz(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<QuizResponse> updateQuiz(@PathVariable Long id, @Valid @RequestBody QuizRequest request) {
        return ResponseEntity.ok(quizService.updateQuiz(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<Void> deleteQuiz(@PathVariable Long id) {
        quizService.deleteQuiz(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/assign/{groupId}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<QuizAssignmentResponse> assignQuiz(
            @PathVariable Long id,
            @PathVariable Long groupId,
            @Valid @RequestBody QuizAssignmentRequest assignmentRequest
    ) {
        return ResponseEntity.ok(quizService.assignQuizToGroup(id, groupId, assignmentRequest));
    }
}