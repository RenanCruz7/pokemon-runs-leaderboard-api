package pokemon.runs.time.leaderboard.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import pokemon.runs.time.leaderboard.domain.run.Run;
import pokemon.runs.time.leaderboard.domain.user.User;
import pokemon.runs.time.leaderboard.dto.runs.AvgRunTimeByGameDTO;
import pokemon.runs.time.leaderboard.dto.runs.RunsCountByGameDTO;
import pokemon.runs.time.leaderboard.repository.run.RunRepository;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("RunRepository - Testes de Persistência")
class RunRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RunRepository runRepository;

    private User testUser;
    private Run testRun1;
    private Run testRun2;

    @BeforeEach
    void setUp() {
        // Criar usuário de teste
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@test.com");
        testUser.setPassword("password");
        testUser.setRole("CUSTOMER");
        entityManager.persist(testUser);

        // Criar primeira run
        testRun1 = new Run();
        testRun1.setGame("Pokemon Red");
        testRun1.setRunTime(Duration.ofHours(2).plusMinutes(30));
        testRun1.setPokedexStatus(151);
        testRun1.setPokemonTeam(Arrays.asList("Pikachu", "Charizard", "Blastoise"));
        testRun1.setObservation("Speed run");
        testRun1.setUser(testUser);
        entityManager.persist(testRun1);

        // Criar segunda run
        testRun2 = new Run();
        testRun2.setGame("Pokemon Blue");
        testRun2.setRunTime(Duration.ofHours(4).plusMinutes(15));
        testRun2.setPokedexStatus(100);
        testRun2.setPokemonTeam(Arrays.asList("Bulbasaur", "Squirtle"));
        testRun2.setObservation("Casual run");
        testRun2.setUser(testUser);
        entityManager.persist(testRun2);

        entityManager.flush();
    }

    @Test
    @DisplayName("Deve salvar uma run com sucesso")
    void testSaveRun_Success() {
        // Arrange
        Run newRun = new Run();
        newRun.setGame("Pokemon Yellow");
        newRun.setRunTime(Duration.ofHours(3));
        newRun.setPokedexStatus(80);
        newRun.setPokemonTeam(Arrays.asList("Pikachu"));
        newRun.setObservation("Test");
        newRun.setUser(testUser);

        // Act
        Run savedRun = runRepository.save(newRun);

        // Assert
        assertNotNull(savedRun.getId());
        assertEquals("Pokemon Yellow", savedRun.getGame());
        assertEquals(Duration.ofHours(3), savedRun.getRunTime());
    }

    @Test
    @DisplayName("Deve buscar run por ID")
    void testFindById_Success() {
        // Act
        Optional<Run> found = runRepository.findById(testRun1.getId());

        // Assert
        assertTrue(found.isPresent());
        assertEquals("Pokemon Red", found.get().getGame());
    }

    @Test
    @DisplayName("Deve retornar empty ao buscar run inexistente")
    void testFindById_NotFound() {
        // Act
        Optional<Run> found = runRepository.findById(9999L);

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Deve buscar runs por game (ignore case)")
    void testFindByGameIgnoreCase_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Run> result = runRepository.findByGameIgnoreCase("pokemon red", pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        assertEquals("Pokemon Red", result.getContent().get(0).getGame());
    }

    @Test
    @DisplayName("Deve buscar runs por user ID")
    void testFindByUserId_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Run> result = runRepository.findByUserId(testUser.getId(), pageable);

        // Assert
        assertEquals(2, result.getTotalElements());
    }

    @Test
    @DisplayName("Deve buscar fastest runs")
    void testFindFastestRuns_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Duration maxDuration = Duration.ofHours(3);

        // Act
        Page<Run> result = runRepository.findFastestRuns(maxDuration, pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        assertEquals("Pokemon Red", result.getContent().get(0).getGame());
    }

    @Test
    @DisplayName("Deve buscar runs por minPokedexStatus")
    void testFindByMinPokedexStatus_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Run> result = runRepository.findByMinPokedexStatus(120, pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        assertEquals(151, result.getContent().get(0).getPokedexStatus());
    }

    @Test
    @DisplayName("Deve buscar runs por pokemon no time")
    void testFindByPokemonInTeam_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Run> result = runRepository.findByPokemonInTeam("Pikachu", pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().get(0).getPokemonTeam().contains("Pikachu"));
    }

    @Test
    @DisplayName("Deve contar runs por game")
    void testCountRunsByGame_Success() {
        // Act
        List<RunsCountByGameDTO> result = runRepository.countRunsByGame();

        // Assert
        assertFalse(result.isEmpty());
        assertTrue(result.size() >= 2);
    }

    @Test
    @DisplayName("Deve calcular tempo médio por game")
    void testAvgRunTimeByGame_Success() {
        // Act
        List<AvgRunTimeByGameDTO> result = runRepository.avgRunTimeByGame();

        // Assert
        assertFalse(result.isEmpty());
        assertTrue(result.size() >= 2);
    }

    @Test
    @DisplayName("Deve buscar top pokemons usados")
    @org.junit.jupiter.api.Disabled("Query usa funções específicas do PostgreSQL (unnest, string_to_array) não suportadas pelo H2")
    void testTopPokemonsUsed_Success() {
        // TESTE DESABILITADO: Query usa funções específicas do PostgreSQL não suportadas pelo H2
        // Este teste só funciona com PostgreSQL em produção

        // Para testar no H2, seria necessário reescrever a query ou mockar o resultado
        // Uncomment below when testing against PostgreSQL:

        /*
        // Act
        List<Object[]> result = runRepository.topPokemonsUsed();

        // Assert
        assertFalse(result.isEmpty());
        // Pikachu deve estar no topo (aparece em 1 run)
        String topPokemon = (String) result.get(0)[0];
        assertTrue(Arrays.asList("Pikachu", "Charizard", "Blastoise", "Bulbasaur", "Squirtle").contains(topPokemon));
        */
    }

    @Test
    @DisplayName("Deve deletar run com sucesso")
    void testDeleteRun_Success() {
        // Arrange
        Long runId = testRun1.getId();

        // Act
        runRepository.delete(testRun1);
        entityManager.flush();

        // Assert
        Optional<Run> found = runRepository.findById(runId);
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Deve atualizar run com sucesso")
    void testUpdateRun_Success() {
        // Arrange
        testRun1.setGame("Pokemon Red Updated");
        testRun1.setPokedexStatus(130);

        // Act
        Run updated = runRepository.save(testRun1);
        entityManager.flush();
        entityManager.clear();

        // Assert
        Optional<Run> found = runRepository.findById(updated.getId());
        assertTrue(found.isPresent());
        assertEquals("Pokemon Red Updated", found.get().getGame());
        assertEquals(130, found.get().getPokedexStatus());
    }

    @Test
    @DisplayName("Deve buscar todas as runs com paginação")
    void testFindAll_WithPagination() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 1);

        // Act
        Page<Run> result = runRepository.findAll(pageable);

        // Assert
        assertEquals(1, result.getContent().size());
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getTotalPages());
    }
}

