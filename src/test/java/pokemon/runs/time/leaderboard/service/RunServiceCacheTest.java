package pokemon.runs.time.leaderboard.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import pokemon.runs.time.leaderboard.domain.run.Run;
import pokemon.runs.time.leaderboard.domain.user.User;
import pokemon.runs.time.leaderboard.dto.runs.CreateRunDTO;
import pokemon.runs.time.leaderboard.dto.runs.PatchRunDTO;
import pokemon.runs.time.leaderboard.dto.runs.RunsCountByGameDTO;
import pokemon.runs.time.leaderboard.repository.run.RunRepository;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {
        "spring.docker.compose.enabled=false",
        "spring.cache.type=simple"
})
@ActiveProfiles("test")
@DisplayName("RunService - Cache e invalidacao")
class RunServiceCacheTest {

    @Autowired
    private RunService runService;

    @Autowired
    private CacheManager cacheManager;

    @MockBean
    private RunRepository runRepository;

    private User testUser;
    private Run testRun;

    @BeforeEach
    void setUp() {
        cacheManager.getCacheNames().forEach(cacheName -> cacheManager.getCache(cacheName).clear());
        Mockito.reset(runRepository);

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("ash");
        testUser.setEmail("ash@pokemon.com");
        testUser.setRole("CUSTOMER");

        testRun = new Run();
        testRun.setId(10L);
        testRun.setGame("Pokemon Red");
        testRun.setRunTime(Duration.ofMinutes(150));
        testRun.setPokedexStatus(151);
        testRun.setPokemonTeam(List.of("Pikachu", "Charizard"));
        testRun.setObservation("Speedrun");
        testRun.setUser(testUser);
    }

    @Test
    @DisplayName("Deve reutilizar cache em GET /runs")
    void getAllRunsCachedUsesCache() {
        var pageable = PageRequest.of(0, 10);
        when(runRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(testRun)));

        var firstResult = runService.getAllRunsCached(pageable);
        var secondResult = runService.getAllRunsCached(pageable);

        assertEquals(1, firstResult.getTotalElements());
        assertEquals(1, secondResult.getTotalElements());
        verify(runRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Deve invalidar caches de leitura ao criar run")
    void createRunEvictsReadCaches() {
        when(runRepository.countRunsByGame()).thenReturn(List.of(new RunsCountByGameDTO("Pokemon Red", 1L)));
        when(runRepository.save(any(Run.class))).thenReturn(testRun);

        assertEquals(1, runService.getRunsCountByGameCached().size());
        assertEquals(1, runService.getRunsCountByGameCached().size());
        verify(runRepository, times(1)).countRunsByGame();

        runService.createRun(new CreateRunDTO("Pokemon Blue", "2:30", 120, List.of("Blastoise"), "Nova run"), testUser);

        assertEquals(1, runService.getRunsCountByGameCached().size());
        verify(runRepository, times(2)).countRunsByGame();
    }

    @Test
    @DisplayName("Deve invalidar caches de leitura ao atualizar run")
    void updateRunEvictsReadCaches() {
        when(runRepository.findAll()).thenReturn(List.of(testRun));
        when(runRepository.findById(anyLong())).thenReturn(java.util.Optional.of(testRun));
        when(runRepository.save(any(Run.class))).thenReturn(testRun);

        assertFalse(runService.getTopPokemonsUsedCached().isEmpty());
        assertFalse(runService.getTopPokemonsUsedCached().isEmpty());
        verify(runRepository, times(1)).findAll();

        runService.updateRun(10L, new PatchRunDTO("Pokemon Red", "2:15", 151, List.of("Pikachu"), "Atualizada"), testUser);

        assertFalse(runService.getTopPokemonsUsedCached().isEmpty());
        verify(runRepository, times(2)).findAll();
    }

    @Test
    @DisplayName("Deve invalidar caches de leitura ao deletar run")
    void deleteRunEvictsReadCaches() {
        var pageable = PageRequest.of(0, 10);
        when(runRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(testRun)));
        when(runRepository.findById(10L)).thenReturn(java.util.Optional.of(testRun));

        assertEquals(1, runService.getAllRunsCached(pageable).getTotalElements());
        assertEquals(1, runService.getAllRunsCached(pageable).getTotalElements());
        verify(runRepository, times(1)).findAll(pageable);

        runService.deleteRun(10L, testUser);

        assertEquals(1, runService.getAllRunsCached(pageable).getTotalElements());
        verify(runRepository, times(2)).findAll(pageable);
    }
}
