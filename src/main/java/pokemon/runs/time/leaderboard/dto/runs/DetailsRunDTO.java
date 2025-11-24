package pokemon.runs.time.leaderboard.dto.runs;

import pokemon.runs.time.leaderboard.domain.run.Run;
import pokemon.runs.time.leaderboard.dto.users.UserSummaryDTO;

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
                formatDuration(run.getRunTime()),
                run.getPokedexStatus(),
                run.getPokemonTeam(),
                run.getObservation(),
                new UserSummaryDTO(run.getUser())
        );
    }

    private static String formatDuration(java.time.Duration duration) {
        if (duration == null) return "00:00";
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        return String.format("%02d:%02d", hours, minutes);
    }
}
