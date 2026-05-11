package QuizMasterUniversity.service;

import QuizMasterUniversity.dto.AuthResponse;
import QuizMasterUniversity.dto.LoginRequest;
import QuizMasterUniversity.dto.RegisterRequest;
import QuizMasterUniversity.dto.UserDto;
import QuizMasterUniversity.entity.StudyGroup;
import QuizMasterUniversity.entity.User;
import QuizMasterUniversity.entity.UserRole;
import QuizMasterUniversity.repository.StudyGroupRepository;
import QuizMasterUniversity.repository.UserRepository;
import QuizMasterUniversity.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final StudyGroupRepository studyGroupRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        String token = jwtService.generateToken(authentication);
        return new AuthResponse(token, loginRequest.getEmail(), getUserRole(authentication));
    }

    public AuthResponse register(RegisterRequest registerRequest) {
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email is already registered");
        }

        StudyGroup group = null;
        if (registerRequest.getRole() == UserRole.STUDENT) {
            if (registerRequest.getGroupId() == null) {
                throw new IllegalArgumentException("Group is required for student registration");
            }
            group = studyGroupRepository.findById(registerRequest.getGroupId())
                    .orElseThrow(() -> new IllegalArgumentException("Study group not found"));
        }

        User user = User.builder()
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(registerRequest.getRole())
                .group(group)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);
        String token = jwtService.generateTokenFromUsername(savedUser.getEmail());
        return new AuthResponse(token, savedUser.getEmail(), savedUser.getRole().name());
    }

    public UserDto getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalStateException("No authenticated user found");
        }

        return userRepository.findByEmail(authentication.getName())
                .map(this::convertToDto)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
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

    private String getUserRole(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .findFirst()
                .map(Object::toString)
                .map(role -> role.replace("ROLE_", ""))
                .orElse("STUDENT");
    }
}
