package pokemon.runs.time.leaderboard.repository;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pokemon.runs.time.leaderboard.domain.run.Run;
import pokemon.runs.time.leaderboard.domain.user.User;
import pokemon.runs.time.leaderboard.dto.runs.TopPokemonDTO;
import pokemon.runs.time.leaderboard.repository.run.RunRepository;
import pokemon.runs.time.leaderboard.repository.user.UserRepository;
import pokemon.runs.time.leaderboard.service.RunService;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
        "spring.docker.compose.enabled=false",
        "spring.flyway.enabled=true",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect"
})
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
@Transactional
@DisplayName("RunRepository - PostgreSQL com Flyway")
class RunRepositoryPostgresTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
    }

    @Autowired
    private Flyway flyway;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RunRepository runRepository;

    @Autowired
    private RunService runService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Flyway aplica todas as migrations no PostgreSQL real")
    void flywayAppliesAllMigrations() {
        assertEquals("7", flyway.info().current().getVersion().getVersion());
    }

    @Test
    @DisplayName("Queries nativas de estatísticas funcionam no PostgreSQL")
    void nativeStatsQueriesWorkOnPostgres() {
        User user = saveUser("postgresuser", "postgres@example.com");
        saveRun(user, "Pokemon Red", Duration.ofMinutes(90), 151, List.of("Pikachu", "Charizard"), "Primeira");
        saveRun(user, "Pokemon Red", Duration.ofMinutes(150), 100, List.of(" Pikachu ", "Bulbasaur"), "Segunda");
        saveRun(user, "Pokemon Blue", Duration.ofMinutes(60), 80, List.of(), "Sem time");

        var countByGame = runRepository.countRunsByGame();
        assertTrue(countByGame.stream().anyMatch(row -> row.game().equals("Pokemon Red") && row.count() == 2));

        var avgByGame = runRepository.avgRunTimeByGame();
        assertTrue(avgByGame.stream().anyMatch(row -> row.game().equals("Pokemon Red") && row.avgRunTime().equals(120.0)));

        List<TopPokemonDTO> topPokemons = runService.getTopPokemonsUsed();
        assertFalse(topPokemons.isEmpty());
        assertEquals("Pikachu", topPokemons.getFirst().pokemon());
        assertEquals(2L, topPokemons.getFirst().count());
        assertTrue(topPokemons.stream().noneMatch(row -> row.pokemon().isBlank()));
    }

    @Test
    @DisplayName("Busca por pokemon evita correspondência parcial no PostgreSQL")
    void pokemonSearchAvoidsPartialMatchesOnPostgres() {
        User user = saveUser("teamuser", "team@example.com");
        saveRun(user, "Pokemon Yellow", Duration.ofMinutes(45), 151, List.of("Pikachu"), "Completa");

        assertEquals(0, runRepository.findByPokemonInTeam("Pika", org.springframework.data.domain.PageRequest.of(0, 10)).getTotalElements());
        assertEquals(1, runRepository.findByPokemonInTeam("Pikachu", org.springframework.data.domain.PageRequest.of(0, 10)).getTotalElements());
    }

    @Test
    @DisplayName("Exportação CSV escapa dados persistidos no PostgreSQL")
    void csvExportEscapesPostgresData() {
        User user = saveUser("csvuser", "csv@example.com");
        saveRun(user, "Pokemon, \"Red\"", Duration.ofMinutes(75), 151, List.of("Mr. Mime", "Farfetch'd"), "Linha 1\nLinha 2");

        String csv = runService.exportRunsToCsv();

        assertTrue(csv.contains("\"Pokemon, \"\"Red\"\"\""));
        assertTrue(csv.contains("Mr. Mime|Farfetch'd"));
        assertTrue(csv.contains("\"Linha 1\nLinha 2\""));
    }

    private User saveUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("password123"));
        user.setRole("CUSTOMER");
        return userRepository.saveAndFlush(user);
    }

    private Run saveRun(User user, String game, Duration runTime, int pokedexStatus, List<String> team, String observation) {
        Run run = new Run();
        run.setGame(game);
        run.setRunTime(runTime);
        run.setPokedexStatus(pokedexStatus);
        run.setPokemonTeam(team);
        run.setObservation(observation);
        run.setUser(user);
        return runRepository.saveAndFlush(run);
    }
}
