package pokemon.runs.time.leaderboard.dto.runs;

import pokemon.runs.time.leaderboard.domain.run.Run;
import pokemon.runs.time.leaderboard.dto.users.UserSummaryDTO;
import pokemon.runs.time.leaderboard.utils.RunTimeParser;

import java.util.List;

public record DetailsRunDTO(
        Long id,
        String game,
        String runTime,
        int pokedexStatus,
        List<String> pokemonTeam,
        String observation,
        UserSummaryDTO user
) {

    public DetailsRunDTO(Run run) {
        this(
                run.getId(),
                run.getGame(),
                RunTimeParser.format(run.getRunTime()),
                run.getPokedexStatus(),
                run.getPokemonTeam(),
                run.getObservation(),
                new UserSummaryDTO(run.getUser())
        );
    }
}
