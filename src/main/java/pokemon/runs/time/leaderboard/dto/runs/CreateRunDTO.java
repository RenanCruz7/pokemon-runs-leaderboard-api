package pokemon.runs.time.leaderboard.dto.runs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateRunDTO(
        @NotBlank(message = "GameName is required")
        @Size(max = 100, message = "Game deve ter no máximo 100 caracteres")
        String game,

        @NotBlank(message = "Run Time is required")
        @Pattern(regexp = "\\d{1,2}:[0-5]\\d", message = "Run time deve estar no formato HH:MM, com minutos entre 00 e 59")
        String runTime,

        @Min(value = 1, message = "Pokedex status deve ser no mínimo 1")
        int pokedexStatus,

        @Size(max = 6, message = "Time deve ter no máximo 6 pokemons")
        List<String> pokemonTeam,

        @Size(max = 100, message = "Observação deve ter no máximo 100 caracteres")
        String observation
) {
}
