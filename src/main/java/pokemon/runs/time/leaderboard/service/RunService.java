package pokemon.runs.time.leaderboard.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pokemon.runs.time.leaderboard.domain.run.Run;
import pokemon.runs.time.leaderboard.dto.runs.CreateRunDTO;
import pokemon.runs.time.leaderboard.dto.runs.PatchRunDTO;
import pokemon.runs.time.leaderboard.dto.runs.TopPokemonDTO;
import pokemon.runs.time.leaderboard.dto.runs.RunsCountByGameDTO;
import pokemon.runs.time.leaderboard.dto.runs.AvgRunTimeByGameDTO;
import pokemon.runs.time.leaderboard.infra.errors.UnauthorizedException;
import pokemon.runs.time.leaderboard.repository.run.RunRepository;
import pokemon.runs.time.leaderboard.domain.user.User;

import java.util.ArrayList;
import java.util.List;
import java.time.Duration;

@Service
public class RunService {
    @Autowired
    private RunRepository runRepository;

    public Run createRun(CreateRunDTO data, User user) {
        if (data.runTime() == null || !data.runTime().matches("\\d{1,2}:\\d{2}")) {
            throw new IllegalArgumentException("Run time deve estar no formato hh:mm");
        }

        if (data.pokedexStatus() < 0) {
            throw new IllegalArgumentException("Pokedex status não pode ser negativo");
        }

        Run run = new Run(data);
        run.setUser(user);
        return runRepository.save(run);
    }

    public Page<Run> getAllRuns(Pageable pageable) {
        return runRepository.findAll(pageable);
    }

    public Run updateRun(Long id, @Valid PatchRunDTO data, User user) {
        var run = runRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Run not found"));

        if (!run.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You are not authorized to update this run");
        }

        if (data.game() != null) {
            run.setGame(data.game());
        }
        if (data.runTime() != null) {
            run.setRunTime(parseDuration(data.runTime()));
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

    private Duration parseDuration(String hhmm) {
        if (hhmm == null || !hhmm.matches("\\d{1,2}:\\d{2}")) return Duration.ZERO;
        String[] parts = hhmm.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        return Duration.ofHours(hours).plusMinutes(minutes);
    }

    public Page<Run> findByGame(String game, Pageable pageable) {
        return runRepository.findByGameIgnoreCase(game, pageable);
    }

    public void deleteRun(Long id, User user) {
        var run = runRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Run not found"));

        if (!run.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You are not authorized to delete this run");
        }

        runRepository.delete(run);
    }

    public Run findById(Long id) {
        return runRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Run com id " + id + " não encontrada"));
    }

    public Page<Run> findFastestRuns(String maxTime, Pageable pageable) {
        Duration maxDuration = parseDuration(maxTime);
        return runRepository.findFastestRuns(maxDuration, pageable);
    }

    public Page<Run> findByMinPokedexStatus(int minStatus, Pageable pageable) {
        return runRepository.findByMinPokedexStatus(minStatus, pageable);
    }

    public Page<Run> findByPokemonInTeam(String pokemon, Pageable pageable) {
        return runRepository.findByPokemonInTeam(pokemon, pageable);
    }

    public List<RunsCountByGameDTO> getRunsCountByGame() {
        return runRepository.countRunsByGame();
    }

    public List<AvgRunTimeByGameDTO> getAvgRunTimeByGame() {
        return runRepository.avgRunTimeByGame();
    }

    public List<TopPokemonDTO> getTopPokemonsUsed() {
        var result = runRepository.topPokemonsUsed();
        List<TopPokemonDTO> list = new ArrayList<>();
        for (Object[] row : result) {
            list.add(new TopPokemonDTO((String) row[0], ((Number) row[1]).longValue()));
        }
        return list;
    }

    public String exportRunsToCsv() {
        List<Run> runs = runRepository.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("id,game,runTime,pokedexStatus,pokemonTeam,observation\n");
        for (Run run : runs) {
            sb.append(run.getId()).append(",")
              .append(run.getGame()).append(",")
              .append(run.getRunTime() != null ? run.getRunTime().toString() : "").append(",")
              .append(run.getPokedexStatus()).append(",")
              .append(run.getPokemonTeam() != null ? String.join("|", run.getPokemonTeam()) : "").append(",")
              .append(run.getObservation() != null ? run.getObservation().replaceAll(",", " ") : "")
              .append("\n");
        }
        return sb.toString();
    }

    public Page<Run> getAllMyRuns(User user, Pageable pageable) {
        var userId = user.getId();
        return runRepository.findByUserId(userId, pageable);
    }
}
