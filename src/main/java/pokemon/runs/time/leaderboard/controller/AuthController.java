package pokemon.runs.time.leaderboard.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pokemon.runs.time.leaderboard.dto.auth.LoginResponseDTO;
import pokemon.runs.time.leaderboard.dto.auth.RegisterResponseDTO;
import pokemon.runs.time.leaderboard.dto.users.CreateUserDTO;
import pokemon.runs.time.leaderboard.dto.users.LoginUserDTO;
import pokemon.runs.time.leaderboard.service.AuthService;

@RestController
@RequestMapping("/auth")
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
}

