package pokemon.runs.time.leaderboard.dto.runs;

import java.io.Serializable;

public record TopPokemonDTO(String pokemon, Long count) implements Serializable {}
