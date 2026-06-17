package pokemon.runs.time.leaderboard.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pokemon.runs.time.leaderboard.dto.integration.PokemonIntegrationResponseDTO;
import pokemon.runs.time.leaderboard.service.PokemonIntegrationService;

@RestController
@RequestMapping("/integrations")
public class IntegrationController {

    @Autowired
    private PokemonIntegrationService pokemonIntegrationService;

    @GetMapping("/pokemon/{pokemon}")
    public ResponseEntity<PokemonIntegrationResponseDTO> getPokemonSummary(@PathVariable String pokemon) {
        if (pokemon == null || pokemon.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do pokemon nao pode estar vazio");
        }

        return ResponseEntity.ok(pokemonIntegrationService.getPokemonSummary(pokemon));
    }
}
