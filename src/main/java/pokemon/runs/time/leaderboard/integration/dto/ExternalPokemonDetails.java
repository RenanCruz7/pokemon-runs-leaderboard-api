package pokemon.runs.time.leaderboard.integration.dto;

import java.util.List;

public record ExternalPokemonDetails(
        String name,
        Long pokedexNumber,
        Integer baseExperience,
        List<String> types
) {
}
