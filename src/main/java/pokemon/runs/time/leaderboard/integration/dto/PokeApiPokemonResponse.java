package pokemon.runs.time.leaderboard.integration.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PokeApiPokemonResponse(
        Long id,
        String name,
        @JsonProperty("base_experience") Integer baseExperience,
        List<PokeApiPokemonTypeSlot> types
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PokeApiPokemonTypeSlot(Integer slot, PokeApiNamedResource type) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PokeApiNamedResource(String name, String url) {
    }
}
