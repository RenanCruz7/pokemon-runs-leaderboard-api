package pokemon.runs.time.leaderboard.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pokemon.runs.time.leaderboard.domain.user.User;
import pokemon.runs.time.leaderboard.dto.auth.LoginResponseDTO;
import pokemon.runs.time.leaderboard.dto.auth.RegisterResponseDTO;
import pokemon.runs.time.leaderboard.dto.users.CreateUserDTO;
import pokemon.runs.time.leaderboard.dto.users.LoginUserDTO;
import pokemon.runs.time.leaderboard.infra.security.TokenService;
import pokemon.runs.time.leaderboard.repository.user.UserRepository;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenService tokenService;

    public RegisterResponseDTO register(CreateUserDTO data) {
        // Verificar se o usuário já existe
        if(userRepository.findByUsername(data.username()).isPresent()) {
            throw new RuntimeException("User already exists");
        }

        if(userRepository.findByEmail(data.email()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        // Criar novo usuário
        User newUser = new User();
        newUser.setUsername(data.username());
        newUser.setPassword(passwordEncoder.encode(data.password()));
        newUser.setEmail(data.email());
        newUser.setRole("CUSTOMER"); // Role padrão

        User savedUser = userRepository.save(newUser);

        return new RegisterResponseDTO(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getRole()
        );
    }

    public LoginResponseDTO login(LoginUserDTO data) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(data.username(), data.password());
        var auth = this.authenticationManager.authenticate(usernamePassword);

        var user = (User) auth.getPrincipal();
        var token = tokenService.generateToken(user);

        return new LoginResponseDTO(
                token,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole()
        );
    }
}

