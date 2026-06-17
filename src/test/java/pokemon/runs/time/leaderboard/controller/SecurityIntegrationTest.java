package pokemon.runs.time.leaderboard.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import pokemon.runs.time.leaderboard.domain.user.PasswordResetToken;
import pokemon.runs.time.leaderboard.dto.runs.CreateRunDTO;
import pokemon.runs.time.leaderboard.dto.users.CreateUserDTO;
import pokemon.runs.time.leaderboard.dto.users.LoginUserDTO;
import pokemon.runs.time.leaderboard.dto.users.RequestPasswordResetDTO;
import pokemon.runs.time.leaderboard.dto.users.ResetPasswordDTO;
import pokemon.runs.time.leaderboard.repository.run.RunRepository;
import pokemon.runs.time.leaderboard.repository.user.PasswordResetTokenRepository;
import pokemon.runs.time.leaderboard.repository.user.UserRepository;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.docker.compose.enabled=false")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Security - Integração com filtros reais")
class SecurityIntegrationTest {

    private static final String TEST_SECRET = "test-secret-key-for-testing-purposes-only";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RunRepository runRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        passwordResetTokenRepository.deleteAll();
        runRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Endpoints públicos funcionam sem token")
    void publicEndpointsDoNotRequireToken() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());

        CreateUserDTO user = new CreateUserDTO("publicuser", "password123", "public@example.com");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("publicuser"));

        LoginUserDTO login = new LoginUserDTO("publicuser", "password123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @DisplayName("Endpoints protegidos rejeitam requisições sem token")
    void protectedEndpointsRequireToken() throws Exception {
        mockMvc.perform(get("/runs"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/integrations/pokemon/pikachu"))
                .andExpect(status().isUnauthorized());

        CreateRunDTO run = new CreateRunDTO(
                "Pokemon Red",
                "2:30",
                151,
                List.of("Pikachu", "Charizard"),
                "Sem token"
        );

        mockMvc.perform(post("/runs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(run)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Fluxo real register/login permite acessar endpoint protegido com JWT")
    void jwtFromLoginAllowsProtectedAccess() throws Exception {
        String token = registerAndLogin("jwtuser", "jwt@example.com", "password123");

        CreateRunDTO run = new CreateRunDTO(
                "Pokemon Crystal",
                "3:45",
                100,
                List.of("Cyndaquil", "Suicune"),
                "JWT real"
        );

        mockMvc.perform(post("/runs")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(run)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.game").value("Pokemon Crystal"));

        mockMvc.perform(get("/runs/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].game").value("Pokemon Crystal"));
    }

    @Test
    @DisplayName("Tokens inválidos e expirados não autenticam endpoints protegidos")
    void invalidAndExpiredTokensDoNotAuthenticate() throws Exception {
        registerAndLogin("expireduser", "expired@example.com", "password123");
        String expiredToken = JWT.create()
                .withIssuer("leaderboard-api")
                .withSubject("expireduser")
                .withExpiresAt(Instant.now().minusSeconds(60))
                .sign(Algorithm.HMAC256(TEST_SECRET));

        mockMvc.perform(get("/runs/me")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/runs/me")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Recuperação de senha funciona sem autenticação e troca credenciais")
    void passwordResetFlowIsPublicAndChangesPassword() throws Exception {
        CreateUserDTO user = new CreateUserDTO("resetuser", "oldpass123", "reset@example.com");
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated());

        RequestPasswordResetDTO request = new RequestPasswordResetDTO("reset@example.com");
        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        PasswordResetToken resetToken = passwordResetTokenRepository.findAll().getFirst();
        ResetPasswordDTO reset = new ResetPasswordDTO(resetToken.getToken(), "newpass123");
        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reset)))
                .andExpect(status().isOk());

        LoginUserDTO oldLogin = new LoginUserDTO("resetuser", "oldpass123");
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(oldLogin)))
                .andExpect(status().isUnauthorized());

        LoginUserDTO newLogin = new LoginUserDTO("resetuser", "newpass123");
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newLogin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    private String registerAndLogin(String username, String email, String password) throws Exception {
        CreateUserDTO user = new CreateUserDTO(username, password, email);
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated());

        LoginUserDTO login = new LoginUserDTO(username, password);
        String response = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        return json.get("token").asText();
    }
}
