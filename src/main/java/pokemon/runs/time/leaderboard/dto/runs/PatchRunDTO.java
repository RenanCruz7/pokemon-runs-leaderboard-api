package pokemon.runs.time.leaderboard.dto.runs;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;

import java.util.List;

public record PatchRunDTO(
        @NotBlank
        String game,

        @NotBlank
        String runTime,

        @Min(value = 1, message = "Pokedex status deve ser no mínimo 1")
        int pokedexStatus,

        List<String> pokemonTeam,

        @Column(length = 100)
        String observation
) {
}
