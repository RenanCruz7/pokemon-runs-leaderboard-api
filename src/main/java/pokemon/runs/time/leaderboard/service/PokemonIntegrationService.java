package pokemon.runs.time.leaderboard.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pokemon.runs.time.leaderboard.dto.integration.PokemonIntegrationResponseDTO;
import pokemon.runs.time.leaderboard.integration.client.NumberConversionSoapClient;
import pokemon.runs.time.leaderboard.integration.client.PokeApiClient;
import pokemon.runs.time.leaderboard.integration.dto.NumberToWordsRequest;

@Service
public class PokemonIntegrationService {

    @Autowired
    private PokeApiClient pokeApiClient;

    @Autowired
    private NumberConversionSoapClient numberConversionSoapClient;

    public PokemonIntegrationResponseDTO getPokemonSummary(String pokemonName) {
        var pokemon = pokeApiClient.fetchPokemon(pokemonName);
        var numberInWords = numberConversionSoapClient.convert(new NumberToWordsRequest(pokemon.pokedexNumber()));

        return new PokemonIntegrationResponseDTO(
                pokemon.name(),
                pokemon.pokedexNumber(),
                numberInWords.words(),
                pokemon.baseExperience(),
                pokemon.types()
        );
    }
}
