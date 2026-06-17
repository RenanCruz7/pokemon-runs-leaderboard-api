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
import org.testcontainers.containers.MySQLContainer;
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
        "api.security.token.secret=test-secret-key-for-mysql"
})
@ActiveProfiles("mysql")
@Testcontainers(disabledWithoutDocker = true)
@Transactional
@DisplayName("RunRepository - MySQL com Flyway")
class RunRepositoryMySqlTest {

    @Container
    static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4")
            .withDatabaseName("leaderboard_db")
            .withUsername("leaderboard")
            .withPassword("leaderboard");

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", mysql::getDriverClassName);
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.MySQLDialect");
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
    @DisplayName("Flyway aplica todas as migrations no MySQL real")
    void flywayAppliesAllMigrations() {
        assertEquals("6", flyway.info().current().getVersion().getVersion());
    }

    @Test
    @DisplayName("Queries e estatisticas funcionam no MySQL")
    void repositoryQueriesAndStatsWorkOnMySql() {
        User user = saveUser("mysqluser", "mysql@example.com");
        saveRun(user, "Pokemon Red", Duration.ofMinutes(90), 151, List.of("Pikachu", "Charizard"), "Primeira");
        saveRun(user, "Pokemon Red", Duration.ofMinutes(150), 100, List.of(" Pikachu ", "Bulbasaur"), "Segunda");
        saveRun(user, "Pokemon Blue", Duration.ofMinutes(60), 80, List.of(), "Sem time");

        assertEquals(0, runRepository.findByPokemonInTeam("Pika", org.springframework.data.domain.PageRequest.of(0, 10)).getTotalElements());
        assertEquals(1, runRepository.findByPokemonInTeam("Bulbasaur", org.springframework.data.domain.PageRequest.of(0, 10)).getTotalElements());

        var countByGame = runRepository.countRunsByGame();
        assertTrue(countByGame.stream().anyMatch(row -> row.game().equals("Pokemon Red") && row.count() == 2));

        var avgByGame = runRepository.avgRunTimeByGame();
        assertTrue(avgByGame.stream().anyMatch(row -> row.game().equals("Pokemon Red") && row.avgRunTime().equals(120.0)));

        List<TopPokemonDTO> topPokemons = runService.getTopPokemonsUsed();
        assertFalse(topPokemons.isEmpty());
        assertEquals("Pikachu", topPokemons.getFirst().pokemon());
        assertEquals(2L, topPokemons.getFirst().count());

        String csv = runService.exportRunsToCsv();
        assertTrue(csv.contains("Pokemon Red"));
        assertTrue(csv.contains("Pikachu|Charizard"));
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
