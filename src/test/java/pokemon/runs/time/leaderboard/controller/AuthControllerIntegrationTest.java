package pokemon.runs.time.leaderboard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import pokemon.runs.time.leaderboard.config.TestSecurityConfig;
import pokemon.runs.time.leaderboard.dto.users.CreateUserDTO;
import pokemon.runs.time.leaderboard.dto.users.LoginUserDTO;
import pokemon.runs.time.leaderboard.repository.user.UserRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("AuthController - Testes de Integração")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
    }

    // ==================== REGISTER TESTS ====================

    @Test
    @DisplayName("POST /auth/register - Deve registrar um novo usuário com sucesso")
    void testRegister_Success() throws Exception {
        CreateUserDTO createUserDTO = new CreateUserDTO(
                "testuser",
                "password123",
                "test@example.com"
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    @DisplayName("POST /auth/register - Deve retornar 400 com username vazio")
    void testRegister_EmptyUsername() throws Exception {
        CreateUserDTO createUserDTO = new CreateUserDTO(
                "",
                "password123",
                "test@example.com"
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/register - Deve retornar 400 com username muito curto (< 3)")
    void testRegister_UsernameShort() throws Exception {
        CreateUserDTO createUserDTO = new CreateUserDTO(
                "ab",
                "password123",
                "test@example.com"
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/register - Deve retornar 400 com username muito longo (> 50)")
    void testRegister_UsernameTooLong() throws Exception {
        CreateUserDTO createUserDTO = new CreateUserDTO(
                "a".repeat(51),
                "password123",
                "test@example.com"
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/register - Deve retornar 400 com password vazio")
    void testRegister_EmptyPassword() throws Exception {
        CreateUserDTO createUserDTO = new CreateUserDTO(
                "testuser",
                "",
                "test@example.com"
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/register - Deve retornar 400 com password muito curto (< 6)")
    void testRegister_PasswordShort() throws Exception {
        CreateUserDTO createUserDTO = new CreateUserDTO(
                "testuser",
                "pass",
                "test@example.com"
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/register - Deve retornar 400 com email vazio")
    void testRegister_EmptyEmail() throws Exception {
        CreateUserDTO createUserDTO = new CreateUserDTO(
                "testuser",
                "password123",
                ""
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/register - Deve retornar 400 com email inválido")
    void testRegister_InvalidEmail() throws Exception {
        CreateUserDTO createUserDTO = new CreateUserDTO(
                "testuser",
                "password123",
                "invalid-email"
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/register - Deve retornar 409 com username duplicado")
    void testRegister_DuplicateUsername() throws Exception {
        // Criar primeiro usuário
        CreateUserDTO firstUser = new CreateUserDTO(
                "testuser",
                "password123",
                "test1@example.com"
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstUser)))
                .andExpect(status().isCreated());

        // Tentar criar outro usuário com mesmo username
        CreateUserDTO secondUser = new CreateUserDTO(
                "testuser",
                "password456",
                "test2@example.com"
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondUser)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value("Recurso duplicado"))
                .andExpect(jsonPath("$.detalhes").value("Nome de usuário já está em uso"));
    }

    @Test
    @DisplayName("POST /auth/register - Deve retornar 409 com email duplicado")
    void testRegister_DuplicateEmail() throws Exception {
        // Criar primeiro usuário
        CreateUserDTO firstUser = new CreateUserDTO(
                "testuser1",
                "password123",
                "test@example.com"
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstUser)))
                .andExpect(status().isCreated());

        // Tentar criar outro usuário com mesmo email
        CreateUserDTO secondUser = new CreateUserDTO(
                "testuser2",
                "password456",
                "test@example.com"
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondUser)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value("Recurso duplicado"))
                .andExpect(jsonPath("$.detalhes").value("Email já está em uso"));
    }

    // ==================== LOGIN TESTS ====================

    @Test
    @DisplayName("POST /auth/login - Deve fazer login com sucesso")
    void testLogin_Success() throws Exception {
        // Registrar usuário primeiro
        CreateUserDTO createUserDTO = new CreateUserDTO(
                "testuser",
                "password123",
                "test@example.com"
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserDTO)))
                .andExpect(status().isCreated());

        // Fazer login
        LoginUserDTO loginUserDTO = new LoginUserDTO(
                "testuser",
                "password123"
        );

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginUserDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    @DisplayName("POST /auth/login - Deve retornar 400 com username vazio")
    void testLogin_EmptyUsername() throws Exception {
        LoginUserDTO loginUserDTO = new LoginUserDTO(
                "",
                "password123"
        );

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginUserDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/login - Deve retornar 400 com password vazio")
    void testLogin_EmptyPassword() throws Exception {
        LoginUserDTO loginUserDTO = new LoginUserDTO(
                "testuser",
                ""
        );

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginUserDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/login - Deve retornar 401 com usuário não encontrado")
    void testLogin_UserNotFound() throws Exception {
        LoginUserDTO loginUserDTO = new LoginUserDTO(
                "nonexistentuser",
                "password123"
        );

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginUserDTO)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value("Credenciais inválidas"));
    }

    @Test
    @DisplayName("POST /auth/login - Deve retornar 401 com password incorreta")
    void testLogin_IncorrectPassword() throws Exception {
        // Registrar usuário
        CreateUserDTO createUserDTO = new CreateUserDTO(
                "testuser",
                "correctpassword",
                "test@example.com"
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserDTO)))
                .andExpect(status().isCreated());

        // Tentar login com senha incorreta
        LoginUserDTO loginUserDTO = new LoginUserDTO(
                "testuser",
                "wrongpassword"
        );

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginUserDTO)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value("Credenciais inválidas"));
    }

    @Test
    @DisplayName("POST /auth/login - Deve retornar token válido para múltiplos logins")
    void testLogin_MultipleLogins() throws Exception {
        // Registrar usuário
        CreateUserDTO createUserDTO = new CreateUserDTO(
                "testuser",
                "password123",
                "test@example.com"
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserDTO)))
                .andExpect(status().isCreated());

        LoginUserDTO loginUserDTO = new LoginUserDTO(
                "testuser",
                "password123"
        );

        // Primeiro login
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginUserDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());

        // Segundo login (deve gerar novo token)
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginUserDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @DisplayName("POST /auth/login - Token deve conter informações do usuário")
    void testLogin_TokenContainsUserInfo() throws Exception {
        // Registrar usuário
        CreateUserDTO createUserDTO = new CreateUserDTO(
                "testuser",
                "password123",
                "test@example.com"
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserDTO)))
                .andExpect(status().isCreated());

        // Login
        LoginUserDTO loginUserDTO = new LoginUserDTO(
                "testuser",
                "password123"
        );

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginUserDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @DisplayName("POST /auth/register - Deve criar usuário com role CUSTOMER padrão")
    void testRegister_DefaultRole() throws Exception {
        CreateUserDTO createUserDTO = new CreateUserDTO(
                "testuser",
                "password123",
                "test@example.com"
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }
}

