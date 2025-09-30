package pokemon.runs.time.leaderboard.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record CreateRunDTO(
        @NotBlank(message = "GameName is required")
        @Column(length = 100)
        String game,

        @NotBlank(message = "Run Time is required")
        String runTime, // formato "hh:mm"

        int pokedexStatus,

        List<String> pokemonTeam,

        @Column(length = 100)
        String observation
) {
}
