package pokemon.runs.time.leaderboard.controller;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import pokemon.runs.time.leaderboard.domain.Run;
import pokemon.runs.time.leaderboard.dto.CreateRunDTO;
import pokemon.runs.time.leaderboard.service.RunService;

@RestController
@RequestMapping("/runs")
public class RunController {

    @Autowired
    private RunService runService;

    @PostMapping()
    @Transactional
    public ResponseEntity<Run> createRun(@RequestBody CreateRunDTO data, UriComponentsBuilder uriBuilder) {
        Run run = runService.createRun(data);
        var uri = uriBuilder.path("/runs/{id}").buildAndExpand(run.getId()).toUri();
        return ResponseEntity.created(uri).body(run);
    }
}
