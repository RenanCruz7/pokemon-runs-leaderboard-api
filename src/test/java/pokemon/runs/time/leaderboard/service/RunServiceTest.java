package pokemon.runs.time.leaderboard.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import pokemon.runs.time.leaderboard.domain.run.Run;
import pokemon.runs.time.leaderboard.domain.user.User;
import pokemon.runs.time.leaderboard.dto.runs.CreateRunDTO;
import pokemon.runs.time.leaderboard.dto.runs.PatchRunDTO;
import pokemon.runs.time.leaderboard.infra.errors.UnauthorizedException;
import pokemon.runs.time.leaderboard.repository.run.RunRepository;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RunService - Testes Unitários")
class RunServiceTest {

    @Mock
    private RunRepository runRepository;

    @InjectMocks
    private RunService runService;

    private User testUser;
    private Run testRun;
    private CreateRunDTO createRunDTO;
    private PatchRunDTO patchRunDTO;

    @BeforeEach
    void setUp() {
        // Criar usuário de teste
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@test.com");
        testUser.setPassword("password");
        testUser.setRole("CUSTOMER");

        // Criar Run de teste
        testRun = new Run();
        testRun.setId(1L);
        testRun.setGame("Pokemon Red");
        testRun.setRunTime(Duration.ofHours(2).plusMinutes(30));
        testRun.setPokedexStatus(151);
        testRun.setPokemonTeam(Arrays.asList("Pikachu", "Charizard", "Blastoise"));
        testRun.setObservation("Speed run");
        testRun.setUser(testUser);

        // Criar DTOs de teste
        createRunDTO = new CreateRunDTO(
                "Pokemon Blue",
                "3:45",
                100,
                Arrays.asList("Bulbasaur", "Squirtle"),
                "First run"
        );

        patchRunDTO = new PatchRunDTO(
                "Pokemon Yellow",
                "2:15",
                120,
                Arrays.asList("Pikachu", "Raichu"),
                "Updated run"
        );
    }

    @Test
    @DisplayName("Deve criar uma run com sucesso")
    void testCreateRun_Success() {
        // Arrange
        when(runRepository.save(any(Run.class))).thenReturn(testRun);

        // Act
        Run result = runService.createRun(createRunDTO, testUser);

        // Assert
        assertNotNull(result);
        assertEquals(testRun.getId(), result.getId());
        verify(runRepository, times(1)).save(any(Run.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar run com formato de tempo inválido")
    void testCreateRun_InvalidTimeFormat() {
        // Arrange
        CreateRunDTO invalidDTO = new CreateRunDTO(
                "Pokemon Red",
                "invalid",
                100,
                Arrays.asList("Pikachu"),
                "Test"
        );

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            runService.createRun(invalidDTO, testUser);
        });
        verify(runRepository, never()).save(any(Run.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar run com pokedexStatus inválido")
    void testCreateRun_InvalidPokedexStatus() {
        // Arrange
        CreateRunDTO invalidDTO = new CreateRunDTO(
                "Pokemon Red",
                "2:30",
                -10,
                Arrays.asList("Pikachu"),
                "Test"
        );

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            runService.createRun(invalidDTO, testUser);
        });
        verify(runRepository, never()).save(any(Run.class));
    }

    @Test
    @DisplayName("Deve buscar todas as runs com paginação")
    void testGetAllRuns_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Run> page = new PageImpl<>(Arrays.asList(testRun));
        when(runRepository.findAll(pageable)).thenReturn(page);

        // Act
        Page<Run> result = runService.getAllRuns(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testRun.getId(), result.getContent().get(0).getId());
        verify(runRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Deve atualizar uma run com sucesso")
    void testUpdateRun_Success() {
        // Arrange
        when(runRepository.findById(1L)).thenReturn(Optional.of(testRun));
        when(runRepository.save(any(Run.class))).thenReturn(testRun);

        // Act
        Run result = runService.updateRun(1L, patchRunDTO, testUser);

        // Assert
        assertNotNull(result);
        verify(runRepository, times(1)).findById(1L);
        verify(runRepository, times(1)).save(any(Run.class));
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException ao atualizar run inexistente")
    void testUpdateRun_NotFound() {
        // Arrange
        when(runRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            runService.updateRun(999L, patchRunDTO, testUser);
        });
        verify(runRepository, times(1)).findById(999L);
        verify(runRepository, never()).save(any(Run.class));
    }

    @Test
    @DisplayName("Deve lançar UnauthorizedException ao atualizar run de outro usuário")
    void testUpdateRun_Unauthorized() {
        // Arrange
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("otheruser");

        when(runRepository.findById(1L)).thenReturn(Optional.of(testRun));

        // Act & Assert
        assertThrows(UnauthorizedException.class, () -> {
            runService.updateRun(1L, patchRunDTO, otherUser);
        });
        verify(runRepository, times(1)).findById(1L);
        verify(runRepository, never()).save(any(Run.class));
    }

    @Test
    @DisplayName("Deve deletar uma run com sucesso")
    void testDeleteRun_Success() {
        // Arrange
        when(runRepository.findById(1L)).thenReturn(Optional.of(testRun));
        doNothing().when(runRepository).delete(testRun);

        // Act
        runService.deleteRun(1L, testUser);

        // Assert
        verify(runRepository, times(1)).findById(1L);
        verify(runRepository, times(1)).delete(testRun);
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException ao deletar run inexistente")
    void testDeleteRun_NotFound() {
        // Arrange
        when(runRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            runService.deleteRun(999L, testUser);
        });
        verify(runRepository, times(1)).findById(999L);
        verify(runRepository, never()).delete(any(Run.class));
    }

    @Test
    @DisplayName("Deve lançar UnauthorizedException ao deletar run de outro usuário")
    void testDeleteRun_Unauthorized() {
        // Arrange
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("otheruser");

        when(runRepository.findById(1L)).thenReturn(Optional.of(testRun));

        // Act & Assert
        assertThrows(UnauthorizedException.class, () -> {
            runService.deleteRun(1L, otherUser);
        });
        verify(runRepository, times(1)).findById(1L);
        verify(runRepository, never()).delete(any(Run.class));
    }

    @Test
    @DisplayName("Deve buscar run por ID com sucesso")
    void testFindById_Success() {
        // Arrange
        when(runRepository.findById(1L)).thenReturn(Optional.of(testRun));

        // Act
        Run result = runService.findById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testRun.getId(), result.getId());
        assertEquals(testRun.getGame(), result.getGame());
        verify(runRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException ao buscar run inexistente por ID")
    void testFindById_NotFound() {
        // Arrange
        when(runRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            runService.findById(999L);
        });
        verify(runRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Deve buscar runs por game com sucesso")
    void testFindByGame_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Run> page = new PageImpl<>(Arrays.asList(testRun));
        when(runRepository.findByGameIgnoreCase("Pokemon Red", pageable)).thenReturn(page);

        // Act
        Page<Run> result = runService.findByGame("Pokemon Red", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(runRepository, times(1)).findByGameIgnoreCase("Pokemon Red", pageable);
    }

    @Test
    @DisplayName("Deve buscar runs fastest com sucesso")
    void testFindFastestRuns_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Run> page = new PageImpl<>(Arrays.asList(testRun));
        Duration maxDuration = Duration.ofHours(5);
        when(runRepository.findFastestRuns(maxDuration, pageable)).thenReturn(page);

        // Act
        Page<Run> result = runService.findFastestRuns("5:00", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(runRepository, times(1)).findFastestRuns(any(Duration.class), eq(pageable));
    }

    @Test
    @DisplayName("Deve buscar runs por minPokedexStatus com sucesso")
    void testFindByMinPokedexStatus_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Run> page = new PageImpl<>(Arrays.asList(testRun));
        when(runRepository.findByMinPokedexStatus(100, pageable)).thenReturn(page);

        // Act
        Page<Run> result = runService.findByMinPokedexStatus(100, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(runRepository, times(1)).findByMinPokedexStatus(100, pageable);
    }

    @Test
    @DisplayName("Deve buscar runs por pokemon no time com sucesso")
    void testFindByPokemonInTeam_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Run> page = new PageImpl<>(Arrays.asList(testRun));
        when(runRepository.findByPokemonInTeam("Pikachu", pageable)).thenReturn(page);

        // Act
        Page<Run> result = runService.findByPokemonInTeam("Pikachu", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(runRepository, times(1)).findByPokemonInTeam("Pikachu", pageable);
    }

    @Test
    @DisplayName("Deve buscar todas as runs do usuário com sucesso")
    void testGetAllMyRuns_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Run> page = new PageImpl<>(Arrays.asList(testRun));
        when(runRepository.findByUserId(1L, pageable)).thenReturn(page);

        // Act
        Page<Run> result = runService.getAllMyRuns(testUser, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(runRepository, times(1)).findByUserId(1L, pageable);
    }

    @Test
    @DisplayName("Deve exportar runs para CSV com sucesso")
    void testExportRunsToCsv_Success() {
        // Arrange
        List<Run> runs = Arrays.asList(testRun);
        when(runRepository.findAll()).thenReturn(runs);

        // Act
        String csv = runService.exportRunsToCsv();

        // Assert
        assertNotNull(csv);
        assertTrue(csv.contains("id,game,runTime,pokedexStatus,pokemonTeam,observation"));
        assertTrue(csv.contains("Pokemon Red"));
        verify(runRepository, times(1)).findAll();
    }
}

