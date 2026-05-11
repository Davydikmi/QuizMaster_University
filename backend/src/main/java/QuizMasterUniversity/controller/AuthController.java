package QuizMasterUniversity.controller;

import QuizMasterUniversity.dto.AuthResponse;
import QuizMasterUniversity.dto.LoginRequest;
import QuizMasterUniversity.dto.RegisterRequest;
import QuizMasterUniversity.dto.UserDto;
import QuizMasterUniversity.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        AuthResponse response = authService.register(registerRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> currentUser() {
        UserDto userDto = authService.getCurrentUser();
        return ResponseEntity.ok(userDto);
    }
}