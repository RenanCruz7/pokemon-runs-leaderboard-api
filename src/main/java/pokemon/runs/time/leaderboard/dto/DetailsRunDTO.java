package pokemon.runs.time.leaderboard.dto;

import pokemon.runs.time.leaderboard.domain.run.Run;

import java.time.Duration;
import java.util.List;

public record DetailsRunDTO(Long id, String game, Duration runTime, int pokedexStatus,
    List<String> pokemonTeam, String observation) {

    public DetailsRunDTO(Run run) {
        this(run.getId(), run.getGame(), run.getRunTime(), run.getPokedexStatus(),
            run.getPokemonTeam(), run.getObservation());
    }
}
