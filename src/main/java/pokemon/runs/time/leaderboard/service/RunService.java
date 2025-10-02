package pokemon.runs.time.leaderboard.service;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pokemon.runs.time.leaderboard.domain.Run;
import pokemon.runs.time.leaderboard.dto.CreateRunDTO;
import pokemon.runs.time.leaderboard.dto.PatchRunDTO;
import pokemon.runs.time.leaderboard.repository.RunRepository;

import java.time.Duration;

@Service
public class RunService {
    @Autowired
    private RunRepository runRepository;

    public Run createRun(CreateRunDTO data) {
        Run run = new Run(data);
        return runRepository.save(run);
    }

    public Page<Run> getAllRuns(Pageable pageable) {
        return runRepository.findAll(pageable);
    }

    public Run updateRun(Long id, @Valid PatchRunDTO data) {
        var run = runRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Run not found"));

        if (data.game() != null) {
            run.setGame(data.game());
        }
        if (data.runTime() != null) {
            run.setRunTime(Duration.parse(data.runTime()));
        }
        if (data.pokedexStatus() >= run.getPokedexStatus()) {
            run.setPokedexStatus(data.pokedexStatus());
        }
        if (data.pokemonTeam() != null) {
            run.setPokemonTeam(data.pokemonTeam());
        }
        if (data.observation() != null) {
            run.setObservation(data.observation());
        }

        return runRepository.save(run);
    }

    public Page<Run> findByGame(String game, Pageable pageable) {
        return runRepository.findByGameIgnoreCase(game, pageable);
    }

    public void deleteRun(Long id) {
        var run = runRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Run not found"));
        runRepository.delete(run);
    }

    public Run findById(Long id) {
        return runRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Run not found"));
    }

    public Page<Run> findFastestRuns(String maxTime, Pageable pageable) {
        Duration maxDuration = Duration.parse(maxTime);
        return runRepository.findFastestRuns(maxDuration, pageable);
    }

    public Page<Run> findByMinPokedexStatus(int minStatus, Pageable pageable) {
        return runRepository.findByMinPokedexStatus(minStatus, pageable);
    }

    public Page<Run> findByPokemonInTeam(String pokemon, Pageable pageable) {
        return runRepository.findByPokemonInTeam(pokemon, pageable);
    }

    public java.util.Map<String, Long> getRunsCountByGame() {
        var result = runRepository.countRunsByGame();
        java.util.Map<String, Long> map = new java.util.HashMap<>();
        for (Object[] row : result) {
            map.put((String) row[0], (Long) row[1]);
        }
        return map;
    }

    public java.util.Map<String, Double> getAvgRunTimeByGame() {
        var result = runRepository.avgRunTimeByGame();
        java.util.Map<String, Double> map = new java.util.HashMap<>();
        for (Object[] row : result) {
            map.put((String) row[0], ((Number) row[1]).doubleValue());
        }
        return map;
    }

    public java.util.List<java.util.Map<String, Object>> getTopPokemonsUsed() {
        var result = runRepository.topPokemonsUsed();
        java.util.List<java.util.Map<String, Object>> list = new java.util.ArrayList<>();
        for (Object[] row : result) {
            java.util.Map<String, Object> entry = new java.util.HashMap<>();
            entry.put("pokemon", row[0]);
            entry.put("count", row[1]);
            list.add(entry);
        }
        return list;
    }
}
