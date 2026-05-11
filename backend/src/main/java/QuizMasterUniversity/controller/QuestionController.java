package QuizMasterUniversity.controller;

import QuizMasterUniversity.dto.QuestionRequest;
import QuizMasterUniversity.dto.QuestionResponse;
import QuizMasterUniversity.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quizzes/{quizId}/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    public ResponseEntity<List<QuestionResponse>> listQuestions(@PathVariable Long quizId) {
        return ResponseEntity.ok(questionService.getQuestionsByQuiz(quizId));
    }

    @GetMapping("/{questionId}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    public ResponseEntity<QuestionResponse> getQuestion(
            @PathVariable Long quizId,
            @PathVariable Long questionId
    ) {
        return ResponseEntity.ok(questionService.getQuestion(quizId, questionId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<QuestionResponse> createQuestion(
            @PathVariable Long quizId,
            @Valid @RequestBody QuestionRequest request
    ) {
        return ResponseEntity.ok(questionService.createQuestion(quizId, request));
    }

    @PutMapping("/{questionId}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<QuestionResponse> updateQuestion(
            @PathVariable Long quizId,
            @PathVariable Long questionId,
            @Valid @RequestBody QuestionRequest request
    ) {
        return ResponseEntity.ok(questionService.updateQuestion(quizId, questionId, request));
    }

    @DeleteMapping("/{questionId}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<Void> deleteQuestion(
            @PathVariable Long quizId,
            @PathVariable Long questionId
    ) {
        questionService.deleteQuestion(quizId, questionId);
        return ResponseEntity.noContent().build();
    }
}
