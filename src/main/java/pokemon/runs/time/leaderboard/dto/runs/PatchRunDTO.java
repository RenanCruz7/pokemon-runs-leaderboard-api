package pokemon.runs.time.leaderboard.dto.runs;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record PatchRunDTO(
        @NotBlank
        String game,

        @NotBlank
        String runTime,

        int pokedexStatus,

        List<String> pokemonTeam,

        @Column(length = 100)
        String observation
) {
}
