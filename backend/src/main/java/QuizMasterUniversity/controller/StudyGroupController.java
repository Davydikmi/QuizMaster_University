package QuizMasterUniversity.controller;

import QuizMasterUniversity.dto.StudyGroupDto;
import QuizMasterUniversity.dto.StudyGroupRequest;
import QuizMasterUniversity.service.StudyGroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@RestController
@RequestMapping("/api/study-groups")
@RequiredArgsConstructor
public class StudyGroupController {

    private final StudyGroupService studyGroupService;

    @GetMapping
    public ResponseEntity<List<StudyGroupDto>> getAllGroups() {
        return ResponseEntity.ok(studyGroupService.getAll());
    }

    @PostMapping
    public ResponseEntity<StudyGroupDto> createGroup(@Valid @RequestBody StudyGroupRequest request) {
        return ResponseEntity.ok(studyGroupService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudyGroupDto> updateGroup(@PathVariable Long id, @Valid @RequestBody StudyGroupRequest request) {
        return ResponseEntity.ok(studyGroupService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long id) {
        studyGroupService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
