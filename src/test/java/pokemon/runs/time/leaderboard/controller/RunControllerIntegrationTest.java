package pokemon.runs.time.leaderboard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import pokemon.runs.time.leaderboard.domain.run.Run;
import pokemon.runs.time.leaderboard.domain.user.User;
import pokemon.runs.time.leaderboard.dto.runs.CreateRunDTO;
import pokemon.runs.time.leaderboard.dto.runs.PatchRunDTO;
import pokemon.runs.time.leaderboard.infra.security.TokenService;
import pokemon.runs.time.leaderboard.repository.run.RunRepository;
import pokemon.runs.time.leaderboard.repository.user.UserRepository;

import java.time.Duration;
import java.util.Arrays;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("RunController - Testes de Integração")
class RunControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RunRepository runRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenService tokenService;

    private User testUser;
    private User otherUser;
    private String testUserToken;
    private String otherUserToken;
    private Run testRun;

    @BeforeEach
    void setUp() {
        // Limpar SecurityContext antes de cada teste
        org.springframework.security.core.context.SecurityContextHolder.clearContext();

        // Limpar banco
        runRepository.deleteAll();
        userRepository.deleteAll();

        // Criar usuário de teste
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@test.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setRole("CUSTOMER");
        testUser = userRepository.save(testUser);

        // Criar outro usuário
        otherUser = new User();
        otherUser.setUsername("otheruser");
        otherUser.setEmail("other@test.com");
        otherUser.setPassword(passwordEncoder.encode("password123"));
        otherUser.setRole("CUSTOMER");
        otherUser = userRepository.save(otherUser);

        // Gerar tokens
        testUserToken = tokenService.generateToken(testUser);
        otherUserToken = tokenService.generateToken(otherUser);

        // Criar run de teste
        testRun = new Run();
        testRun.setGame("Pokemon Red");
        testRun.setRunTime(Duration.ofHours(2).plusMinutes(30));
        testRun.setPokedexStatus(151);
        testRun.setPokemonTeam(Arrays.asList("Pikachu", "Charizard", "Blastoise"));
        testRun.setObservation("Speed run");
        testRun.setUser(testUser);
        testRun = runRepository.save(testRun);
    }

    /**
     * Helper method para configurar o SecurityContext com um usuário autenticado
     * Necessário porque @AutoConfigureMockMvc(addFilters = false) desabilita o SecurityFilter
     */
    private void authenticateUser(User user) {
        org.springframework.security.core.context.SecurityContextHolder.getContext()
                .setAuthentication(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        user, null, user.getAuthorities()));
    }

    /**
     * Helper method para limpar o SecurityContext após o teste
     */
    private void clearAuthentication() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("POST /runs - Deve criar uma run com sucesso")
    void testCreateRun_Success() throws Exception {
        authenticateUser(testUser);

        CreateRunDTO createRunDTO = new CreateRunDTO(
                "Pokemon Blue",
                "3:45",
                100,
                Arrays.asList("Bulbasaur", "Squirtle"),
                "First run"
        );

        mockMvc.perform(post("/runs")
                        .header("Authorization", "Bearer " + testUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRunDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.game").value("Pokemon Blue"))
                .andExpect(jsonPath("$.runTime").value("03:45"))
                .andExpect(jsonPath("$.pokedexStatus").value(100))
                .andExpect(jsonPath("$.pokemonTeam", hasSize(2)))
                .andExpect(jsonPath("$.observation").value("First run"));

        clearAuthentication();
    }

    @Test
    @DisplayName("POST /runs - Deve retornar 400 com dados inválidos")
    void testCreateRun_InvalidData() throws Exception {
        authenticateUser(testUser);

        CreateRunDTO createRunDTO = new CreateRunDTO(
                "",  // game vazio
                "invalid",  // formato inválido
                0,  // pokedexStatus inválido (deve ser >= 1)
                Arrays.asList("Pikachu", "Raichu"),
                "Test"
        );

        mockMvc.perform(post("/runs")
                        .header("Authorization", "Bearer " + testUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRunDTO)))
                .andExpect(status().isBadRequest());

        clearAuthentication();
    }

    @Test
    @DisplayName("POST /runs - Deve retornar 401 sem autenticação")
    void testCreateRun_Unauthorized() throws Exception {
        CreateRunDTO createRunDTO = new CreateRunDTO(
                "Pokemon Blue",
                "3:45",
                100,
                Arrays.asList("Bulbasaur", "Squirtle"),
                "Test"
        );

        mockMvc.perform(post("/runs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRunDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /runs - Deve retornar todas as runs com paginação")
    void testGetAllRuns_Success() throws Exception {
        mockMvc.perform(get("/runs")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].game").value("Pokemon Red"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /runs/me - Deve retornar apenas runs do usuário autenticado")
    void testGetAllMyRuns_Success() throws Exception {
        authenticateUser(testUser);

        // Criar run para outro usuário
        Run otherRun = new Run();
        otherRun.setGame("Pokemon Gold");
        otherRun.setRunTime(Duration.ofHours(4));
        otherRun.setPokedexStatus(100);
        otherRun.setPokemonTeam(Arrays.asList("Cyndaquil", "Typhlosion"));
        otherRun.setObservation("Test");
        otherRun.setUser(otherUser);
        runRepository.save(otherRun);

        mockMvc.perform(get("/runs/me")
                        .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].game").value("Pokemon Red"));

        clearAuthentication();
    }

    @Test
    @DisplayName("GET /runs/{id} - Deve retornar run por ID")
    void testGetRunById_Success() throws Exception {
        mockMvc.perform(get("/runs/" + testRun.getId())
                        .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testRun.getId()))
                .andExpect(jsonPath("$.game").value("Pokemon Red"))
                .andExpect(jsonPath("$.pokedexStatus").value(151));
    }

    @Test
    @DisplayName("GET /runs/{id} - Deve retornar 404 para run inexistente")
    void testGetRunById_NotFound() throws Exception {
        mockMvc.perform(get("/runs/9999")
                        .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("Recurso não encontrado"));
    }

    @Test
    @DisplayName("PATCH /runs/{id} - Deve atualizar run com sucesso")
    void testUpdateRun_Success() throws Exception {
        authenticateUser(testUser);

        PatchRunDTO patchRunDTO = new PatchRunDTO(
                "Pokemon Yellow",
                "2:15",
                151,  // Atualizar com um valor maior ou igual ao anterior (151)
                Arrays.asList("Pikachu", "Raichu"),
                "Updated run"
        );

        mockMvc.perform(patch("/runs/" + testRun.getId())
                        .header("Authorization", "Bearer " + testUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchRunDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.game").value("Pokemon Yellow"))
                .andExpect(jsonPath("$.runTime").value("02:15"))
                .andExpect(jsonPath("$.pokedexStatus").value(151));

        clearAuthentication();
    }

    @Test
    @DisplayName("PATCH /runs/{id} - Deve retornar 403 ao tentar atualizar run de outro usuário")
    void testUpdateRun_Forbidden() throws Exception {
        authenticateUser(otherUser);

        PatchRunDTO patchRunDTO = new PatchRunDTO(
                "Pokemon Yellow",
                "2:15",
                120,
                Arrays.asList("Pikachu", "Raichu"),
                "Test"
        );

        mockMvc.perform(patch("/runs/" + testRun.getId())
                        .header("Authorization", "Bearer " + otherUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchRunDTO)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.erro").value("Não autorizado"));

        clearAuthentication();
    }

    @Test
    @DisplayName("DELETE /runs/{id} - Deve deletar run com sucesso")
    void testDeleteRun_Success() throws Exception {
        // Como addFilters=false, precisamos configurar o SecurityContext manualmente
        org.springframework.security.core.context.SecurityContextHolder.getContext()
                .setAuthentication(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        testUser, null, testUser.getAuthorities()));

        mockMvc.perform(delete("/runs/" + testRun.getId())
                        .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isNoContent());

        // Verificar que foi deletado
        mockMvc.perform(get("/runs/" + testRun.getId())
                        .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isNotFound());

        // Limpar o SecurityContext
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("DELETE /runs/{id} - Deve retornar 403 ao tentar deletar run de outro usuário")
    void testDeleteRun_Forbidden() throws Exception {
        authenticateUser(otherUser);

        mockMvc.perform(delete("/runs/" + testRun.getId())
                        .header("Authorization", "Bearer " + otherUserToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.erro").value("Não autorizado"));

        clearAuthentication();
    }

    @Test
    @DisplayName("GET /runs/game/{game} - Deve buscar runs por game")
    void testGetRunsByGame_Success() throws Exception {
        mockMvc.perform(get("/runs/game/Pokemon Red")
                        .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].game").value("Pokemon Red"));
    }

    @Test
    @DisplayName("GET /runs/fastest - Deve buscar runs mais rápidas")
    void testGetFastestRuns_Success() throws Exception {
        mockMvc.perform(get("/runs/fastest")
                        .header("Authorization", "Bearer " + testUserToken)
                        .param("maxTime", "5:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    @DisplayName("GET /runs/fastest - Deve retornar 400 com formato de tempo inválido")
    void testGetFastestRuns_InvalidTimeFormat() throws Exception {
        mockMvc.perform(get("/runs/fastest")
                        .header("Authorization", "Bearer " + testUserToken)
                        .param("maxTime", "invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Argumento inválido"));
    }

    @Test
    @DisplayName("GET /runs/pokedex - Deve buscar runs por pokedex status")
    void testGetByPokedexStatus_Success() throws Exception {
        mockMvc.perform(get("/runs/pokedex")
                        .header("Authorization", "Bearer " + testUserToken)
                        .param("minStatus", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    @DisplayName("GET /runs/pokedex - Deve retornar 400 com minStatus inválido")
    void testGetByPokedexStatus_InvalidMinStatus() throws Exception {
        mockMvc.perform(get("/runs/pokedex")
                        .header("Authorization", "Bearer " + testUserToken)
                        .param("minStatus", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Argumento inválido"));
    }

    @Test
    @DisplayName("GET /runs/team - Deve buscar runs por pokemon no time")
    void testGetByPokemonInTeam_Success() throws Exception {
        mockMvc.perform(get("/runs/team")
                        .header("Authorization", "Bearer " + testUserToken)
                        .param("pokemon", "Pikachu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    @DisplayName("GET /runs/team - Deve retornar 400 com nome vazio")
    void testGetByPokemonInTeam_EmptyName() throws Exception {
        mockMvc.perform(get("/runs/team")
                        .header("Authorization", "Bearer " + testUserToken)
                        .param("pokemon", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Argumento inválido"));
    }

    @Test
    @DisplayName("GET /runs/export/csv - Deve exportar runs em CSV")
    void testExportRunsToCsv_Success() throws Exception {
        mockMvc.perform(get("/runs/export/csv")
                        .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_PLAIN))
                .andExpect(header().string("Content-Disposition", "attachment; filename=runs.csv"))
                .andExpect(content().string(containsString("id,game,runTime,pokedexStatus,pokemonTeam,observation")))
                .andExpect(content().string(containsString("Pokemon Red")));
    }
}

