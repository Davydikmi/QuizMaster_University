package QuizMasterUniversity.service;

import QuizMasterUniversity.dto.StudyGroupDto;
import QuizMasterUniversity.dto.StudyGroupRequest;
import QuizMasterUniversity.entity.StudyGroup;
import QuizMasterUniversity.entity.User;
import QuizMasterUniversity.repository.AttemptAnswerRepository;
import QuizMasterUniversity.repository.QuizAssignmentRepository;
import QuizMasterUniversity.repository.QuizAttemptRepository;
import QuizMasterUniversity.repository.StudyGroupRepository;
import QuizMasterUniversity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudyGroupService {
    private final StudyGroupRepository studyGroupRepository;
    private final UserRepository userRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final AttemptAnswerRepository attemptAnswerRepository;
    private final QuizAssignmentRepository quizAssignmentRepository;

    public List<StudyGroupDto> getAll() {
        return studyGroupRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public StudyGroupDto create(StudyGroupRequest request) {
        StudyGroup group = StudyGroup.builder()
                .name(request.getName())
                .courseNumber(request.getCourseNumber())
                .speciality(request.getSpeciality())
                .createdAt(LocalDateTime.now())
                .build();
        return toDto(studyGroupRepository.save(group));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public StudyGroupDto update(Long id, StudyGroupRequest request) {
        StudyGroup group = studyGroupRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Study group not found"));
        group.setName(request.getName());
        group.setCourseNumber(request.getCourseNumber());
        group.setSpeciality(request.getSpeciality());
        return toDto(studyGroupRepository.save(group));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void delete(Long id) {
        StudyGroup group = studyGroupRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Study group not found"));
        List<User> students = userRepository.findByGroupId(group.getId());
        var attempts = students.stream()
                .flatMap(student -> quizAttemptRepository.findByStudentId(student.getId()).stream())
                .toList();
        if (!attempts.isEmpty()) {
            var attemptIds = attempts.stream().map(attempt -> attempt.getId()).toList();
            attemptAnswerRepository.deleteSelectedOptionsByAttemptIds(attemptIds);
            attemptAnswerRepository.deleteByAttemptIds(attemptIds);
            quizAttemptRepository.deleteAll(attempts);
        }
        userRepository.deleteAll(students);
        quizAssignmentRepository.deleteByGroupId(group.getId());
        studyGroupRepository.delete(group);
    }

    private StudyGroupDto toDto(StudyGroup group) {
        return StudyGroupDto.builder()
                .id(group.getId())
                .name(group.getName())
                .courseNumber(group.getCourseNumber())
                .speciality(group.getSpeciality())
                .build();
    }
}
