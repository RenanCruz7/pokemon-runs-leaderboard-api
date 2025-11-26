package pokemon.runs.time.leaderboard.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import pokemon.runs.time.leaderboard.domain.user.User;
import pokemon.runs.time.leaderboard.dto.auth.LoginResponseDTO;
import pokemon.runs.time.leaderboard.dto.auth.RegisterResponseDTO;
import pokemon.runs.time.leaderboard.dto.users.CreateUserDTO;
import pokemon.runs.time.leaderboard.dto.users.LoginUserDTO;
import pokemon.runs.time.leaderboard.infra.errors.DuplicateResourceException;
import pokemon.runs.time.leaderboard.infra.security.TokenService;
import pokemon.runs.time.leaderboard.repository.user.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService - Testes Unitários")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private AuthService authService;

    private CreateUserDTO validCreateUserDTO;
    private LoginUserDTO validLoginUserDTO;
    private User testUser;

    @BeforeEach
    void setUp() {
        validCreateUserDTO = new CreateUserDTO(
                "testuser",
                "password123",
                "test@example.com"
        );

        validLoginUserDTO = new LoginUserDTO(
                "testuser",
                "password123"
        );

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setRole("CUSTOMER");
    }

    // ==================== REGISTER TESTS ====================

    @Test
    @DisplayName("Register - Deve registrar usuário com sucesso")
    void testRegister_Success() {
        // Arrange
        when(userRepository.findByUsername(validCreateUserDTO.username())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(validCreateUserDTO.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(validCreateUserDTO.password())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        RegisterResponseDTO response = authService.register(validCreateUserDTO);

        // Assert
        assertNotNull(response);
        assertEquals("testuser", response.username());
        assertEquals("test@example.com", response.email());
        assertEquals("CUSTOMER", response.role());
        assertNotNull(response.id());

        verify(userRepository, times(1)).findByUsername(validCreateUserDTO.username());
        verify(userRepository, times(1)).findByEmail(validCreateUserDTO.email());
        verify(passwordEncoder, times(1)).encode(validCreateUserDTO.password());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Register - Deve lançar exceção com username duplicado")
    void testRegister_DuplicateUsername() {
        // Arrange
        when(userRepository.findByUsername(validCreateUserDTO.username()))
                .thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(DuplicateResourceException.class,
                () -> authService.register(validCreateUserDTO));

        verify(userRepository, times(1)).findByUsername(validCreateUserDTO.username());
        verify(userRepository, never()).findByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Register - Deve lançar exceção com email duplicado")
    void testRegister_DuplicateEmail() {
        // Arrange
        when(userRepository.findByUsername(validCreateUserDTO.username())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(validCreateUserDTO.email())).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(DuplicateResourceException.class,
                () -> authService.register(validCreateUserDTO));

        verify(userRepository, times(1)).findByUsername(validCreateUserDTO.username());
        verify(userRepository, times(1)).findByEmail(validCreateUserDTO.email());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Register - Deve lançar exceção com username muito curto")
    void testRegister_UsernameTooShort() {
        // Arrange
        CreateUserDTO shortUsernameDTO = new CreateUserDTO(
                "ab",
                "password123",
                "test@example.com"
        );

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> authService.register(shortUsernameDTO));
    }

    @Test
    @DisplayName("Register - Deve lançar exceção com password muito curta")
    void testRegister_PasswordTooShort() {
        // Arrange
        CreateUserDTO shortPasswordDTO = new CreateUserDTO(
                "testuser",
                "pass",
                "test@example.com"
        );

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> authService.register(shortPasswordDTO));
    }

    @Test
    @DisplayName("Register - Deve criar usuário com role CUSTOMER padrão")
    void testRegister_DefaultRole() {
        // Arrange
        when(userRepository.findByUsername(validCreateUserDTO.username())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(validCreateUserDTO.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(validCreateUserDTO.password())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        RegisterResponseDTO response = authService.register(validCreateUserDTO);

        // Assert
        assertEquals("CUSTOMER", response.role());
    }

    @Test
    @DisplayName("Register - Deve codificar a password antes de salvar")
    void testRegister_PasswordEncoded() {
        // Arrange
        when(userRepository.findByUsername(validCreateUserDTO.username())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(validCreateUserDTO.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(validCreateUserDTO.password())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        authService.register(validCreateUserDTO);

        // Assert
        verify(passwordEncoder, times(1)).encode("password123");
    }

    // ==================== LOGIN TESTS ====================

    @Test
    @DisplayName("Login - Deve fazer login com sucesso")
    void testLogin_Success() {
        // Arrange
        UsernamePasswordAuthenticationToken authenticatedToken =
                new UsernamePasswordAuthenticationToken(
                        testUser,
                        null,
                        testUser.getAuthorities()
                );

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authenticatedToken);
        when(tokenService.generateToken(testUser)).thenReturn("jwt-token");

        // Act
        LoginResponseDTO response = authService.login(validLoginUserDTO);

        // Assert
        assertNotNull(response);
        assertEquals("jwt-token", response.token());
        assertEquals(testUser.getId(), response.id());
        assertEquals("testuser", response.username());
        assertEquals("test@example.com", response.email());
        assertEquals("CUSTOMER", response.role());

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenService, times(1)).generateToken(testUser);
    }

    @Test
    @DisplayName("Login - Deve gerar token JWT válido")
    void testLogin_GeneratesValidToken() {
        // Arrange
        UsernamePasswordAuthenticationToken authenticatedToken =
                new UsernamePasswordAuthenticationToken(
                        testUser,
                        null,
                        testUser.getAuthorities()
                );

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authenticatedToken);
        when(tokenService.generateToken(testUser)).thenReturn("valid-jwt-token");

        // Act
        LoginResponseDTO response = authService.login(validLoginUserDTO);

        // Assert
        assertNotNull(response.token());
        assertFalse(response.token().isEmpty());
        assertEquals("valid-jwt-token", response.token());
    }

    @Test
    @DisplayName("Login - Deve retornar dados do usuário no token")
    void testLogin_ResponseContainsUserData() {
        // Arrange
        UsernamePasswordAuthenticationToken authenticatedToken =
                new UsernamePasswordAuthenticationToken(
                        testUser,
                        null,
                        testUser.getAuthorities()
                );

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authenticatedToken);
        when(tokenService.generateToken(testUser)).thenReturn("jwt-token");

        // Act
        LoginResponseDTO response = authService.login(validLoginUserDTO);

        // Assert
        assertEquals(testUser.getId(), response.id());
        assertEquals(testUser.getUsername(), response.username());
        assertEquals(testUser.getEmail(), response.email());
        assertEquals(testUser.getRole(), response.role());
    }

    @Test
    @DisplayName("Login - Deve autenticar com AuthenticationManager")
    void testLogin_AuthenticationManagerCalled() {
        // Arrange
        UsernamePasswordAuthenticationToken authenticatedToken =
                new UsernamePasswordAuthenticationToken(
                        testUser,
                        null,
                        testUser.getAuthorities()
                );

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authenticatedToken);
        when(tokenService.generateToken(testUser)).thenReturn("jwt-token");

        // Act
        authService.login(validLoginUserDTO);

        // Assert
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("Login - Deve gerar token para cada login")
    void testLogin_TokenGeneratedPerLogin() {
        // Arrange
        UsernamePasswordAuthenticationToken authenticatedToken =
                new UsernamePasswordAuthenticationToken(
                        testUser,
                        null,
                        testUser.getAuthorities()
                );

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authenticatedToken);
        when(tokenService.generateToken(testUser))
                .thenReturn("token1")
                .thenReturn("token2");

        // Act
        LoginResponseDTO response1 = authService.login(validLoginUserDTO);
        LoginResponseDTO response2 = authService.login(validLoginUserDTO);

        // Assert
        assertEquals("token1", response1.token());
        assertEquals("token2", response2.token());
        assertNotEquals(response1.token(), response2.token());

        verify(tokenService, times(2)).generateToken(testUser);
    }
}

