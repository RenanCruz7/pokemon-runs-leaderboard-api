package pokemon.runs.time.leaderboard.dto.runs;

import java.io.Serializable;

public record RunsCountByGameDTO(String game, Long count) implements Serializable {}
