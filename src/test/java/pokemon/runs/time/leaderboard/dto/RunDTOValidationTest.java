package pokemon.runs.time.leaderboard.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pokemon.runs.time.leaderboard.dto.runs.CreateRunDTO;
import pokemon.runs.time.leaderboard.dto.runs.PatchRunDTO;

import java.util.Arrays;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Run DTOs - Testes de Validação")
class RunDTOValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("CreateRunDTO - Deve validar com sucesso com dados corretos")
    void testCreateRunDTO_ValidData() {
        // Arrange
        CreateRunDTO dto = new CreateRunDTO(
                "Pokemon Red",
                "2:30",
                151,
                Arrays.asList("Pikachu", "Charizard"),
                "Speed run"
        );

        // Act
        Set<ConstraintViolation<CreateRunDTO>> violations = validator.validate(dto);

        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("CreateRunDTO - Deve falhar quando game está em branco")
    void testCreateRunDTO_BlankGame() {
        // Arrange
        CreateRunDTO dto = new CreateRunDTO(
                "",
                "2:30",
                151,
                Arrays.asList("Pikachu"),
                "Test"
        );

        // Act
        Set<ConstraintViolation<CreateRunDTO>> violations = validator.validate(dto);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("game")));
    }

    @Test
    @DisplayName("CreateRunDTO - Deve falhar quando runTime está em branco")
    void testCreateRunDTO_BlankRunTime() {
        // Arrange
        CreateRunDTO dto = new CreateRunDTO(
                "Pokemon Red",
                "",
                151,
                Arrays.asList("Pikachu"),
                "Test"
        );

        // Act
        Set<ConstraintViolation<CreateRunDTO>> violations = validator.validate(dto);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("runTime")));
    }

    @Test
    @DisplayName("CreateRunDTO - Deve permitir valores opcionais nulos")
    void testCreateRunDTO_NullOptionalFields() {
        // Arrange
        CreateRunDTO dto = new CreateRunDTO(
                "Pokemon Red",
                "2:30",
                1,  // Mínimo válido é 1
                null,
                null
        );

        // Act
        Set<ConstraintViolation<CreateRunDTO>> violations = validator.validate(dto);

        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("PatchRunDTO - Deve validar com sucesso com dados corretos")
    void testPatchRunDTO_ValidData() {
        // Arrange
        PatchRunDTO dto = new PatchRunDTO(
                "Pokemon Blue",
                "3:45",
                100,
                Arrays.asList("Bulbasaur", "Squirtle"),
                "Updated run"
        );

        // Act
        Set<ConstraintViolation<PatchRunDTO>> violations = validator.validate(dto);

        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("PatchRunDTO - Deve falhar quando game está em branco")
    void testPatchRunDTO_BlankGame() {
        // Arrange
        PatchRunDTO dto = new PatchRunDTO(
                "",
                "2:30",
                100,
                Arrays.asList("Pikachu"),
                "Test"
        );

        // Act
        Set<ConstraintViolation<PatchRunDTO>> violations = validator.validate(dto);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("game")));
    }

    @Test
    @DisplayName("PatchRunDTO - Deve falhar quando runTime está em branco")
    void testPatchRunDTO_BlankRunTime() {
        // Arrange
        PatchRunDTO dto = new PatchRunDTO(
                "Pokemon Red",
                "",
                100,
                Arrays.asList("Pikachu"),
                "Test"
        );

        // Act
        Set<ConstraintViolation<PatchRunDTO>> violations = validator.validate(dto);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("runTime")));
    }

    @Test
    @DisplayName("CreateRunDTO - Deve criar DTO com lista vazia de pokemon")
    void testCreateRunDTO_EmptyPokemonList() {
        // Arrange
        CreateRunDTO dto = new CreateRunDTO(
                "Pokemon Red",
                "2:30",
                151,
                Arrays.asList(),
                "No team"
        );

        // Act
        Set<ConstraintViolation<CreateRunDTO>> violations = validator.validate(dto);

        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("CreateRunDTO - Deve criar DTO com múltiplos pokemons")
    void testCreateRunDTO_MultiplePokemon() {
        // Arrange
        CreateRunDTO dto = new CreateRunDTO(
                "Pokemon Red",
                "2:30",
                151,
                Arrays.asList("Pikachu", "Charizard", "Blastoise", "Venusaur", "Alakazam", "Dragonite"),
                "Full team"
        );

        // Act
        Set<ConstraintViolation<CreateRunDTO>> violations = validator.validate(dto);

        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("CreateRunDTO - Deve rejeitar pokedexStatus zero")
    void testCreateRunDTO_ZeroPokedexStatus() {
        // Arrange
        CreateRunDTO dto = new CreateRunDTO(
                "Pokemon Red",
                "2:30",
                0,  // Inválido - deve ser >= 1
                Arrays.asList("Pikachu"),
                "Starting"
        );

        // Act
        Set<ConstraintViolation<CreateRunDTO>> violations = validator.validate(dto);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("pokedexStatus")));
    }

    @Test
    @DisplayName("CreateRunDTO - Deve aceitar pokedexStatus com valor 1 (mínimo)")
    void testCreateRunDTO_MinimumPokedexStatus() {
        // Arrange
        CreateRunDTO dto = new CreateRunDTO(
                "Pokemon Red",
                "2:30",
                1,  // Valor mínimo válido
                Arrays.asList("Pikachu"),
                "Starting"
        );

        // Act
        Set<ConstraintViolation<CreateRunDTO>> violations = validator.validate(dto);

        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("CreateRunDTO - Deve aceitar observation nula")
    void testCreateRunDTO_NullObservation() {
        // Arrange
        CreateRunDTO dto = new CreateRunDTO(
                "Pokemon Red",
                "2:30",
                151,
                Arrays.asList("Pikachu"),
                null
        );

        // Act
        Set<ConstraintViolation<CreateRunDTO>> violations = validator.validate(dto);

        // Assert
        assertTrue(violations.isEmpty());
    }
}

