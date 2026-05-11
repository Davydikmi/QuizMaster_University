package QuizMasterUniversity.service;

import QuizMasterUniversity.dto.UserDto;
import QuizMasterUniversity.entity.StudyGroup;
import QuizMasterUniversity.entity.User;
import QuizMasterUniversity.entity.UserRole;
import QuizMasterUniversity.repository.StudyGroupRepository;
import QuizMasterUniversity.repository.AttemptAnswerRepository;
import QuizMasterUniversity.repository.QuizAttemptRepository;
import QuizMasterUniversity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final StudyGroupRepository studyGroupRepository;
    private final PasswordEncoder passwordEncoder;
    private final QuizAttemptRepository quizAttemptRepository;
    private final AttemptAnswerRepository attemptAnswerRepository;

    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public Optional<UserDto> getUserById(Long id) {
        return userRepository.findById(id).map(this::convertToDto);
    }

    public UserDto createUser(UserDto userDto) {
        User user = convertToEntity(userDto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);
        return convertToDto(savedUser);
    }

    public Optional<UserDto> updateUser(Long id, UserDto userDto) {
        return userRepository.findById(id).map(user -> {
            boolean adminRequest = isCurrentUserAdmin();
            user.setFirstName(userDto.getFirstName());
            user.setLastName(userDto.getLastName());
            user.setEmail(userDto.getEmail());

            if (adminRequest) {
                user.setRole(UserRole.valueOf(userDto.getRole()));
                user.setActive(userDto.isEnabled());

                if (user.getRole() == UserRole.STUDENT) {
                    if (userDto.getGroupId() == null) {
                        throw new IllegalArgumentException("Student must belong to a study group");
                    }
                    StudyGroup group = studyGroupRepository.findById(userDto.getGroupId())
                            .orElseThrow(() -> new IllegalArgumentException("Study group not found"));
                    user.setGroup(group);
                } else {
                    user.setGroup(null);
                }
            }

            if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(userDto.getPassword()));
            }
            User updatedUser = userRepository.save(user);
            return convertToDto(updatedUser);
        });
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        var attempts = quizAttemptRepository.findByStudentId(user.getId());
        if (!attempts.isEmpty()) {
            var attemptIds = attempts.stream().map(attempt -> attempt.getId()).toList();
            attemptAnswerRepository.deleteSelectedOptionsByAttemptIds(attemptIds);
            attemptAnswerRepository.deleteByAttemptIds(attemptIds);
            quizAttemptRepository.deleteAll(attempts);
        }
        userRepository.delete(user);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean isCurrentUser(Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            return false;
        }

        return userRepository.findById(userId)
                .map(user -> user.getEmail().equalsIgnoreCase(authentication.getName()))
                .orElse(false);
    }

    private UserDto convertToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .enabled(user.getActive())
                .groupId(user.getGroup() != null ? user.getGroup().getId() : null)
                .groupName(user.getGroup() != null ? user.getGroup().getName() : null)
                .build();
    }

    private User convertToEntity(UserDto userDto) {
        return User.builder()
                .firstName(userDto.getFirstName())
                .lastName(userDto.getLastName())
                .email(userDto.getEmail())
                .password(userDto.getPassword())
                .role(UserRole.valueOf(userDto.getRole()))
                .active(userDto.isEnabled())
                .group(userDto.getGroupId() != null ? studyGroupRepository.findById(userDto.getGroupId()).orElse(null) : null)
                .build();
    }

    private boolean isCurrentUserAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }
}
