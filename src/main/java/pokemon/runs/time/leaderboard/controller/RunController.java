package pokemon.runs.time.leaderboard.controller;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
}
