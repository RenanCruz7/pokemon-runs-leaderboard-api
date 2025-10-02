package pokemon.runs.time.leaderboard.controller;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import pokemon.runs.time.leaderboard.domain.Run;
import pokemon.runs.time.leaderboard.dto.CreateRunDTO;
import pokemon.runs.time.leaderboard.dto.DetailsRunDTO;
import pokemon.runs.time.leaderboard.dto.PatchRunDTO;
import pokemon.runs.time.leaderboard.service.RunService;

@RestController
@RequestMapping("/runs")
public class RunController {

    @Autowired
    private RunService runService;

    @PostMapping()
    @Transactional
    public ResponseEntity createRun(@RequestBody CreateRunDTO data, UriComponentsBuilder uriBuilder) {
        Run run = runService.createRun(data);
        var uri = uriBuilder.path("/runs/{id}").buildAndExpand(run.getId()).toUri();
        return ResponseEntity.created(uri).body(new DetailsRunDTO(run));
    }

    @GetMapping
    public ResponseEntity<Page<Run>> getAllRuns(@PageableDefault(size = 10) Pageable pageable) {
        var runs = runService.getAllRuns(pageable);
        return ResponseEntity.ok(runs);
    }

    @PatchMapping("/{id}")
    @Transactional
    public ResponseEntity updateRun(@PathVariable Long id, @RequestBody PatchRunDTO data) {
        var run = runService.updateRun(id, data);
        return ResponseEntity.ok(new DetailsRunDTO(run));
    }

    @GetMapping("/game/{game}")
    public ResponseEntity<Page<Run>> getRunsByGame(
            @PathVariable String game,
            @PageableDefault(size = 10) Pageable pageable) {
        var runs = runService.findByGame(game, pageable);
        return ResponseEntity.ok(runs);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity deleteRun(@PathVariable Long id) {
        runService.deleteRun(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DetailsRunDTO> getRunById(@PathVariable Long id) {
        var run = runService.findById(id);
        return ResponseEntity.ok(new DetailsRunDTO(run));
    }

    @GetMapping("/fastest")
    public ResponseEntity<Page<DetailsRunDTO>> getFastestRuns(
            @RequestParam String maxTime,
            @PageableDefault(size = 10, sort = "runTime") Pageable pageable) {
        var runs = runService.findFastestRuns(maxTime, pageable);
        return ResponseEntity.ok(runs.map(DetailsRunDTO::new));
    }

    @GetMapping("/pokedex")
    public ResponseEntity<Page<DetailsRunDTO>> getByPokedexStatus(
            @RequestParam int minStatus,
            @PageableDefault(size = 10, sort = "pokedexStatus", direction = Sort.Direction.DESC) Pageable pageable) {
        var runs = runService.findByMinPokedexStatus(minStatus, pageable);
        return ResponseEntity.ok(runs.map(DetailsRunDTO::new));
    }

    @GetMapping("/team")
    public ResponseEntity<Page<DetailsRunDTO>> getByPokemonInTeam(
            @RequestParam String pokemon,
            @PageableDefault(size = 10) Pageable pageable) {
        var runs = runService.findByPokemonInTeam(pokemon, pageable);
        return ResponseEntity.ok(runs.map(DetailsRunDTO::new));
    }

    @GetMapping("/stats/count-by-game")
    public ResponseEntity<java.util.Map<String, Long>> getRunsCountByGame() {
        return ResponseEntity.ok(runService.getRunsCountByGame());
    }

    @GetMapping("/stats/avg-time-by-game")
    public ResponseEntity<java.util.Map<String, Double>> getAvgRunTimeByGame() {
        return ResponseEntity.ok(runService.getAvgRunTimeByGame());
    }

    @GetMapping("/stats/top-pokemons")
    public ResponseEntity<java.util.List<java.util.Map<String, Object>>> getTopPokemonsUsed() {
        return ResponseEntity.ok(runService.getTopPokemonsUsed());
    }
}
