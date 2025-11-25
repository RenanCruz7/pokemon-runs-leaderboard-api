package pokemon.runs.time.leaderboard.controller;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import pokemon.runs.time.leaderboard.domain.run.Run;
import pokemon.runs.time.leaderboard.domain.user.User;
import pokemon.runs.time.leaderboard.dto.runs.CreateRunDTO;
import pokemon.runs.time.leaderboard.dto.runs.DetailsRunDTO;
import pokemon.runs.time.leaderboard.dto.runs.PatchRunDTO;
import pokemon.runs.time.leaderboard.dto.runs.TopPokemonDTO;
import pokemon.runs.time.leaderboard.dto.runs.RunsCountByGameDTO;
import pokemon.runs.time.leaderboard.dto.runs.AvgRunTimeByGameDTO;
import pokemon.runs.time.leaderboard.service.RunService;

import java.util.List;

@RestController
@RequestMapping("/runs")
public class RunController {

    @Autowired
    private RunService runService;

    @PostMapping()
    @Transactional
    public ResponseEntity<DetailsRunDTO> createRun(@RequestBody CreateRunDTO data,
                                                    @AuthenticationPrincipal User user,
                                                    UriComponentsBuilder uriBuilder) {
        Run run = runService.createRun(data, user);
        var uri = uriBuilder.path("/runs/{id}").buildAndExpand(run.getId()).toUri();
        return ResponseEntity.created(uri).body(new DetailsRunDTO(run));
    }

    @GetMapping
    public ResponseEntity<Page<DetailsRunDTO>> getAllRuns(@PageableDefault(size = 10) Pageable pageable) {
        var runs = runService.getAllRuns(pageable);
        return ResponseEntity.ok(runs.map(DetailsRunDTO::new));
    }

    @GetMapping("/me")
    public ResponseEntity<Page<DetailsRunDTO>> getAllMyRuns(@AuthenticationPrincipal User user,
                                                   @PageableDefault(size = 10) Pageable pageable) {
        var runs = runService.getAllMyRuns(user, pageable);
        return ResponseEntity.ok(runs.map(DetailsRunDTO::new));
    }

    @PatchMapping("/{id}")
    @Transactional
    public ResponseEntity<DetailsRunDTO> updateRun(@PathVariable Long id,
                                                    @RequestBody PatchRunDTO data,
                                                    @AuthenticationPrincipal User user) {
        var run = runService.updateRun(id, data, user);
        return ResponseEntity.ok(new DetailsRunDTO(run));
    }

    @GetMapping("/game/{game}")
    public ResponseEntity<Page<DetailsRunDTO>> getRunsByGame(
            @PathVariable String game,
            @PageableDefault(size = 10) Pageable pageable) {
        var runs = runService.findByGame(game, pageable);
        return ResponseEntity.ok(runs.map(DetailsRunDTO::new));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deleteRun(@PathVariable Long id,
                                          @AuthenticationPrincipal User user) {
        runService.deleteRun(id, user);
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
    public ResponseEntity<List<RunsCountByGameDTO>> getRunsCountByGame() {
        return ResponseEntity.ok(runService.getRunsCountByGame());
    }

    @GetMapping("/stats/avg-time-by-game")
    public ResponseEntity<List<AvgRunTimeByGameDTO>> getAvgRunTimeByGame() {
        return ResponseEntity.ok(runService.getAvgRunTimeByGame());
    }

    @GetMapping("/stats/top-pokemons")
    public ResponseEntity<List<TopPokemonDTO>> getTopPokemonsUsed() {
        return ResponseEntity.ok(runService.getTopPokemonsUsed());
    }

    @GetMapping("/export/csv")
    public ResponseEntity<String> exportRunsToCsv() {
        String csv = runService.exportRunsToCsv();
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=runs.csv");
        headers.setContentType(MediaType.TEXT_PLAIN);
        return ResponseEntity.ok().headers(headers).body(csv);
    }
}
