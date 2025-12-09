package pokemon.runs.time.leaderboard.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pokemon.runs.time.leaderboard.domain.user.PasswordResetToken;
import pokemon.runs.time.leaderboard.domain.user.User;
import pokemon.runs.time.leaderboard.dto.auth.LoginResponseDTO;
import pokemon.runs.time.leaderboard.dto.auth.RegisterResponseDTO;
import pokemon.runs.time.leaderboard.dto.users.*;
import pokemon.runs.time.leaderboard.infra.errors.DuplicateResourceException;
import pokemon.runs.time.leaderboard.infra.errors.NotFoundException;
import pokemon.runs.time.leaderboard.infra.errors.UnauthorizedException;
import pokemon.runs.time.leaderboard.infra.security.TokenService;
import pokemon.runs.time.leaderboard.repository.user.PasswordResetTokenRepository;
import pokemon.runs.time.leaderboard.repository.user.UserRepository;

import java.time.LocalDateTime;
import java.util.UUID;

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

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

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
        newUser.setRole("CUSTOMER");

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

    @Transactional
    public MessageResponseDTO changePassword(User user, ChangePasswordDTO data) {
        if (!passwordEncoder.matches(data.currentPassword(), user.getPassword())) {
            throw new UnauthorizedException("Senha atual incorreta");
        }

        if (data.newPassword().length() < 6) {
            throw new IllegalArgumentException("Nova senha deve ter no mínimo 6 caracteres");
        }

        user.setPassword(passwordEncoder.encode(data.newPassword()));
        userRepository.save(user);

        return new MessageResponseDTO("Senha alterada com sucesso");
    }

    @Transactional
    public MessageResponseDTO requestPasswordReset(RequestPasswordResetDTO data) {
        User user = userRepository.findByEmail(data.email())
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        passwordResetTokenRepository.deleteByUserId(user.getId());

        String token = UUID.randomUUID().toString();

        LocalDateTime expiryDate = LocalDateTime.now().plusHours(1);

        PasswordResetToken resetToken = new PasswordResetToken(token, user, expiryDate);
        passwordResetTokenRepository.save(resetToken);

        return new MessageResponseDTO("Se o email existir, um link de recuperação será enviado");
    }

    @Transactional
    public MessageResponseDTO resetPassword(ResetPasswordDTO data) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(data.token())
                .orElseThrow(() -> new NotFoundException("Token inválido ou expirado"));

        if (resetToken.isUsed()) {
            throw new UnauthorizedException("Token já foi utilizado");
        }

        if (resetToken.isExpired()) {
            throw new UnauthorizedException("Token expirado");
        }

        if (data.newPassword().length() < 6) {
            throw new IllegalArgumentException("Nova senha deve ter no mínimo 6 caracteres");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(data.newPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        return new MessageResponseDTO("Senha redefinida com sucesso");
    }
}

