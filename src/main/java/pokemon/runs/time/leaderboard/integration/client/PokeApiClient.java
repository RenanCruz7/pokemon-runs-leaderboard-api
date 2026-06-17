package pokemon.runs.time.leaderboard.integration.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import pokemon.runs.time.leaderboard.integration.dto.ExternalPokemonDetails;
import pokemon.runs.time.leaderboard.integration.dto.PokeApiPokemonResponse;
import pokemon.runs.time.leaderboard.infra.errors.ExternalServiceException;
import pokemon.runs.time.leaderboard.infra.errors.NotFoundException;

import java.net.SocketTimeoutException;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Component
public class PokeApiClient {

    private static final Logger log = LoggerFactory.getLogger(PokeApiClient.class);
    private static final String SERVICE_NAME = "PokeAPI";

    @Autowired
    private RestClient pokeApiRestClient;

    @Autowired
    private ObjectMapper objectMapper;

    public ExternalPokemonDetails fetchPokemon(String pokemonName) {
        String normalizedPokemonName = pokemonName.trim().toLowerCase(Locale.ROOT);

        try {
            return pokeApiRestClient.get()
                    .uri("/pokemon/{name}", normalizedPokemonName)
                    .exchange((request, response) -> {
                        HttpStatusCode statusCode = response.getStatusCode();

                        if (statusCode.value() == HttpStatus.NOT_FOUND.value()) {
                            throw new NotFoundException("Pokemon '" + normalizedPokemonName + "' nao encontrado no servico REST externo");
                        }

                        if (!statusCode.is2xxSuccessful()) {
                            throw new ExternalServiceException(
                                    HttpStatus.BAD_GATEWAY,
                                    SERVICE_NAME,
                                    "Servico REST externo retornou status " + statusCode.value()
                            );
                        }

                        try {
                            PokeApiPokemonResponse body = objectMapper.readValue(response.getBody(), PokeApiPokemonResponse.class);
                            return mapResponse(body);
                        } catch (Exception ex) {
                            log.warn("Resposta invalida ao consumir {}: {}", SERVICE_NAME, ex.getMessage());
                            throw new ExternalServiceException(HttpStatus.BAD_GATEWAY, SERVICE_NAME, "Resposta invalida recebida do servico REST externo");
                        }
                    });
        } catch (NotFoundException | ExternalServiceException ex) {
            throw ex;
        } catch (ResourceAccessException ex) {
            throw mapResourceAccessException(ex);
        }
    }

    private ExternalPokemonDetails mapResponse(PokeApiPokemonResponse body) {
        if (body == null || body.id() == null || body.name() == null || body.types() == null || body.types().isEmpty()) {
            log.warn("Resposta incompleta recebida do {}", SERVICE_NAME);
            throw new ExternalServiceException(HttpStatus.BAD_GATEWAY, SERVICE_NAME, "Resposta invalida recebida do servico REST externo");
        }

        List<String> types = body.types().stream()
                .filter(typeSlot -> typeSlot != null && typeSlot.type() != null && typeSlot.type().name() != null)
                .sorted(Comparator.comparingInt(typeSlot -> typeSlot.slot() == null ? Integer.MAX_VALUE : typeSlot.slot()))
                .map(typeSlot -> typeSlot.type().name())
                .toList();

        if (types.isEmpty()) {
            log.warn("Resposta sem tipos validos recebida do {}", SERVICE_NAME);
            throw new ExternalServiceException(HttpStatus.BAD_GATEWAY, SERVICE_NAME, "Resposta invalida recebida do servico REST externo");
        }

        return new ExternalPokemonDetails(body.name(), body.id(), body.baseExperience(), types);
    }

    private ExternalServiceException mapResourceAccessException(ResourceAccessException ex) {
        Throwable cause = ex.getMostSpecificCause();
        if (cause instanceof SocketTimeoutException) {
            log.warn("Timeout ao consumir {}: {}", SERVICE_NAME, ex.getMessage());
            return new ExternalServiceException(HttpStatus.GATEWAY_TIMEOUT, SERVICE_NAME, "Timeout ao consumir servico REST externo");
        }

        log.warn("Falha de comunicacao ao consumir {}: {}", SERVICE_NAME, ex.getMessage());
        return new ExternalServiceException(HttpStatus.BAD_GATEWAY, SERVICE_NAME, "Falha ao consumir servico REST externo");
    }
}
