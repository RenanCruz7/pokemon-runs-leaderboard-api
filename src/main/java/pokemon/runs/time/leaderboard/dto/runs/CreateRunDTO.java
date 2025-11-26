package pokemon.runs.time.leaderboard.dto.runs;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;

import java.util.List;

public record CreateRunDTO(
        @NotBlank(message = "GameName is required")
        @Column(length = 100)
        String game,

        @NotBlank(message = "Run Time is required")
        String runTime, // formato "hh:mm"

        @Min(value = 1, message = "Pokedex status deve ser no mínimo 1")
        int pokedexStatus,

        List<String> pokemonTeam,

        @Column(length = 100)
        String observation
) {
}
