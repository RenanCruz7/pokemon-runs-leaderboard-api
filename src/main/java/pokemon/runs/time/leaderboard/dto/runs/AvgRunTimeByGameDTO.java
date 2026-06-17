package pokemon.runs.time.leaderboard.dto.runs;

import java.io.Serializable;

public record AvgRunTimeByGameDTO(String game, Double avgRunTime) implements Serializable {}
