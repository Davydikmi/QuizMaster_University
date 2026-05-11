package QuizMasterUniversity.service;

import QuizMasterUniversity.dto.StudyGroupDto;
import QuizMasterUniversity.dto.StudyGroupRequest;
import QuizMasterUniversity.entity.StudyGroup;
import QuizMasterUniversity.repository.StudyGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudyGroupService {
    private final StudyGroupRepository studyGroupRepository;

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
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
    public void delete(Long id) {
        studyGroupRepository.deleteById(id);
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
