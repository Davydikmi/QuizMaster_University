package QuizMasterUniversity.service;

import QuizMasterUniversity.dto.CourseRequest;
import QuizMasterUniversity.dto.CourseResponse;
import QuizMasterUniversity.entity.Course;
import QuizMasterUniversity.entity.User;
import QuizMasterUniversity.repository.CourseRepository;
import QuizMasterUniversity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    @PreAuthorize("hasRole('ADMIN')")
    public CourseResponse createCourse(CourseRequest request) {
        User teacher = getAuthenticatedUser();

        Course course = Course.builder()
                .name(request.getName())
                .description(request.getDescription())
                .teacher(teacher)
                .createdAt(LocalDateTime.now())
                .build();

        Course savedCourse = courseRepository.save(course);
        return convertToResponse(savedCourse);
    }

    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public Page<CourseResponse> getCourses(Pageable pageable) {
        if (isCurrentUserAdmin()) {
            return courseRepository.findAll(pageable).map(this::convertToResponse);
        }
        String email = getCurrentUserEmail();
        return courseRepository.findByTeacherEmail(email, pageable).map(this::convertToResponse);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public CourseResponse getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));
        return convertToResponse(course);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public CourseResponse updateCourse(Long id, CourseRequest request) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));
        course.setName(request.getName());
        course.setDescription(request.getDescription());

        Course updatedCourse = courseRepository.save(course);
        return convertToResponse(updatedCourse);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));
        courseRepository.delete(course);
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
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }

    private User getAuthenticatedUser() {
        String email = getCurrentUserEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
    }

    private CourseResponse convertToResponse(Course course) {
        return CourseResponse.builder()
                .id(course.getId())
                .name(course.getName())
                .description(course.getDescription())
                .teacherId(course.getTeacher().getId())
                .teacherEmail(course.getTeacher().getEmail())
                .createdAt(course.getCreatedAt())
                .build();
    }
}
