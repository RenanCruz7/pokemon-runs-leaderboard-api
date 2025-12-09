package pokemon.runs.time.leaderboard.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pokemon.runs.time.leaderboard.domain.user.User;
import pokemon.runs.time.leaderboard.dto.auth.LoginResponseDTO;
import pokemon.runs.time.leaderboard.dto.auth.RegisterResponseDTO;
import pokemon.runs.time.leaderboard.dto.users.*;
import pokemon.runs.time.leaderboard.service.AuthService;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDTO> register(@RequestBody @Valid CreateUserDTO data) {
        RegisterResponseDTO response = authService.register(data);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid LoginUserDTO data) {
        LoginResponseDTO response = authService.login(data);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/change-password")
    public ResponseEntity<MessageResponseDTO> changePassword(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid ChangePasswordDTO data) {
        MessageResponseDTO response = authService.changePassword(user, data);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponseDTO> requestPasswordReset(
            @RequestBody @Valid RequestPasswordResetDTO data) {
        MessageResponseDTO response = authService.requestPasswordReset(data);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponseDTO> resetPassword(
            @RequestBody @Valid ResetPasswordDTO data) {
        MessageResponseDTO response = authService.resetPassword(data);
        return ResponseEntity.ok(response);
    }
}

