package pokemon.runs.time.leaderboard.dto.integration;

import java.util.List;

public record PokemonIntegrationResponseDTO(
        String pokemon,
        Long pokedexNumber,
        String pokedexNumberInWords,
        Integer baseExperience,
        List<String> types
) {
}
