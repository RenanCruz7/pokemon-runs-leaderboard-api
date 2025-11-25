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
import pokemon.runs.time.leaderboard.infra.errors.DuplicateResourceException;
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
        if (data.username().length() < 3) {
            throw new IllegalArgumentException("Username deve ter no mínimo 3 caracteres");
        }

        if (data.password().length() < 6) {
            throw new IllegalArgumentException("Senha deve ter no mínimo 6 caracteres");
        }

        if(userRepository.findByUsername(data.username()).isPresent()) {
            throw new DuplicateResourceException("Nome de usuário já está em uso");
        }

        if(userRepository.findByEmail(data.email()).isPresent()) {
            throw new DuplicateResourceException("Email já está em uso");
        }

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

