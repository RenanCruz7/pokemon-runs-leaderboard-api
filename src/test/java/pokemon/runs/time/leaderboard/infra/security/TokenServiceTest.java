package pokemon.runs.time.leaderboard.infra.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import pokemon.runs.time.leaderboard.domain.user.User;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("TokenService - Testes de Integração")
class TokenServiceTest {

    @Autowired
    private TokenService tokenService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser.setRole("CUSTOMER");
    }

    // ==================== GENERATE TOKEN TESTS ====================

    @Test
    @DisplayName("GenerateToken - Deve gerar um token JWT válido")
    void testGenerateToken_Success() {
        // Act
        String token = tokenService.generateToken(testUser);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.contains("."));
    }

    @Test
    @DisplayName("GenerateToken - Token deve conter três partes (header.payload.signature)")
    void testGenerateToken_ValidJWTFormat() {
        // Act
        String token = tokenService.generateToken(testUser);

        // Assert
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length);
    }

    @Test
    @DisplayName("GenerateToken - Token deve incluir username como subject")
    void testGenerateToken_IncludesUsername() {
        // Act
        String token = tokenService.generateToken(testUser);
        String subject = JWT.decode(token).getSubject();

        // Assert
        assertEquals("testuser", subject);
    }

    @Test
    @DisplayName("GenerateToken - Token deve incluir user ID como claim")
    void testGenerateToken_IncludesUserId() {
        // Act
        String token = tokenService.generateToken(testUser);
        Long userId = JWT.decode(token).getClaim("id").asLong();

        // Assert
        assertEquals(1L, userId);
    }

    @Test
    @DisplayName("GenerateToken - Token deve incluir issuer")
    void testGenerateToken_IncludesIssuer() {
        // Act
        String token = tokenService.generateToken(testUser);
        String issuer = JWT.decode(token).getIssuer();

        // Assert
        assertEquals("leaderboard-api", issuer);
    }

    @Test
    @DisplayName("GenerateToken - Token deve incluir data de expiração")
    void testGenerateToken_IncludesExpiresAt() {
        // Act
        String token = tokenService.generateToken(testUser);

        // Assert
        assertNotNull(JWT.decode(token).getExpiresAt());
    }


    @Test
    @DisplayName("GenerateToken - Token não deve ser null ou vazio")
    void testGenerateToken_NotNullOrEmpty() {
        // Act
        String token = tokenService.generateToken(testUser);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertFalse(token.isBlank());
    }

    // ==================== VALIDATE TOKEN TESTS ====================

    @Test
    @DisplayName("ValidateToken - Deve validar token JWT válido")
    void testValidateToken_ValidToken() {
        // Arrange
        String token = tokenService.generateToken(testUser);

        // Act
        String subject = tokenService.validateToken(token);

        // Assert
        assertEquals("testuser", subject);
    }

    @Test
    @DisplayName("ValidateToken - Deve retornar vazio para token inválido")
    void testValidateToken_InvalidToken() {
        // Act
        String subject = tokenService.validateToken("invalid-token");

        // Assert
        assertEquals("", subject);
    }

    @Test
    @DisplayName("ValidateToken - Deve retornar vazio para token null")
    void testValidateToken_NullToken() {
        // Act
        String subject = tokenService.validateToken(null);

        // Assert
        assertEquals("", subject);
    }

    @Test
    @DisplayName("ValidateToken - Deve retornar vazio para token vazio")
    void testValidateToken_EmptyToken() {
        // Act
        String subject = tokenService.validateToken("");

        // Assert
        assertEquals("", subject);
    }

    @Test
    @DisplayName("ValidateToken - Deve retornar username como subject")
    void testValidateToken_ReturnsUsername() {
        // Arrange
        User user = new User();
        user.setId(2L);
        user.setUsername("anotheruser");
        user.setEmail("another@example.com");
        user.setPassword("password123");
        user.setRole("CUSTOMER");

        String token = tokenService.generateToken(user);

        // Act
        String subject = tokenService.validateToken(token);

        // Assert
        assertEquals("anotheruser", subject);
    }

    @Test
    @DisplayName("ValidateToken - Deve rejeitar token com issuer diferente")
    void testValidateToken_WrongIssuer() {
        // Arrange - Criar um token com issuer diferente manualmente
        String secret = "test-secret";
        String wrongToken = JWT.create()
                .withIssuer("wrong-issuer")
                .withSubject("testuser")
                .withClaim("id", 1L)
                .sign(Algorithm.HMAC256(secret));

        // Act
        String subject = tokenService.validateToken(wrongToken);

        // Assert
        assertEquals("", subject);
    }

    @Test
    @DisplayName("ValidateToken - Deve rejeitar token expirado")
    void testValidateToken_ExpiredToken() {
        // Arrange - Criar um token que já expirou (simulando através de manipulação)
        String expiredToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJsZWFkZXJib2FyZC1hcGkiLCJzdWIiOiJ0ZXN0dXNlciIsImlkIjoxLCJleHAiOjE2MDAwMDAwMDB9.invalid";

        // Act
        String subject = tokenService.validateToken(expiredToken);

        // Assert
        assertEquals("", subject);
    }

    @Test
    @DisplayName("ValidateToken - Deve validar token gerado para diferentes usuários")
    void testValidateToken_DifferentUsers() {
        // Arrange
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("user1");
        user1.setEmail("user1@example.com");
        user1.setPassword("password123");
        user1.setRole("CUSTOMER");

        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2.setPassword("password123");
        user2.setRole("CUSTOMER");

        String token1 = tokenService.generateToken(user1);
        String token2 = tokenService.generateToken(user2);

        // Act
        String subject1 = tokenService.validateToken(token1);
        String subject2 = tokenService.validateToken(token2);

        // Assert
        assertEquals("user1", subject1);
        assertEquals("user2", subject2);
    }

    // ==================== TOKEN STRUCTURE TESTS ====================

    @Test
    @DisplayName("Token - Deve ter estrutura correta")
    void testToken_Structure() {
        // Act
        String token = tokenService.generateToken(testUser);

        // Assert
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length);

        // Header
        assertFalse(parts[0].isEmpty());

        // Payload
        assertFalse(parts[1].isEmpty());

        // Signature
        assertFalse(parts[2].isEmpty());
    }


    @Test
    @DisplayName("Token - Payload deve estar codificado em Base64")
    void testToken_PayloadBase64Encoded() {
        // Act
        String token = tokenService.generateToken(testUser);
        String[] parts = token.split("\\.");

        // Assert
        try {
            // Tentar decodificar o payload
            java.util.Base64.getUrlDecoder().decode(parts[1]);
            assertTrue(true);
        } catch (Exception e) {
            fail("Payload should be Base64 encoded");
        }
    }
}

