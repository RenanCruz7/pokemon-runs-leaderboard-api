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
import pokemon.runs.time.leaderboard.utils.RunTimeParser;

import java.util.ArrayList;
import java.util.List;
import java.time.Duration;

@Service
public class RunService {
    @Autowired
    private RunRepository runRepository;

    public Run createRun(CreateRunDTO data, User user) {
        Duration runTime = RunTimeParser.parse(data.runTime());

        if (data.pokedexStatus() < 1) {
            throw new IllegalArgumentException("Pokedex status deve ser no mínimo 1");
        }

        Run run = new Run();
        run.setGame(data.game());
        run.setRunTime(runTime);
        run.setPokedexStatus(data.pokedexStatus());
        run.setPokemonTeam(data.pokemonTeam());
        run.setObservation(data.observation());
        run.setUser(user);
        return runRepository.save(run);
    }

    public Page<Run> getAllRuns(Pageable pageable) {
        return runRepository.findAll(pageable);
    }

    public Run updateRun(Long id, @Valid PatchRunDTO data, User user) {
        var run = runRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Run com id " + id + " não encontrada"));

        // Verifica se o usuário autenticado é o dono da run
        if (!run.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("Você não tem permissão para atualizar esta run");
        }

        if (data.game() != null) {
            run.setGame(data.game());
        }
        if (data.runTime() != null) {
            run.setRunTime(RunTimeParser.parse(data.runTime()));
        }
        if (data.pokedexStatus() != null) {
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

    public void deleteRun(Long id, User user) {
        var run = runRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Run com id " + id + " não encontrada"));

        // Verifica se o usuário autenticado é o dono da run
        if (!run.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("Você não tem permissão para deletar esta run");
        }

        runRepository.delete(run);
    }

    public Run findById(Long id) {
        return runRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Run com id " + id + " não encontrada"));
    }

    public Page<Run> findFastestRuns(String maxTime, Pageable pageable) {
        Duration maxDuration = RunTimeParser.parse(maxTime);
        return runRepository.findFastestRuns(maxDuration, pageable);
    }

    public Page<Run> findByMinPokedexStatus(int minStatus, Pageable pageable) {
        return runRepository.findByMinPokedexStatus(minStatus, pageable);
    }

    public Page<Run> findByPokemonInTeam(String pokemon, Pageable pageable) {
        return runRepository.findByPokemonInTeam(pokemon.trim(), pageable);
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
            sb.append(csvValue(run.getId())).append(",")
              .append(csvValue(run.getGame())).append(",")
              .append(csvValue(RunTimeParser.format(run.getRunTime()))).append(",")
              .append(csvValue(run.getPokedexStatus())).append(",")
              .append(csvValue(run.getPokemonTeam() != null ? String.join("|", run.getPokemonTeam()) : "")).append(",")
              .append(csvValue(run.getObservation()))
              .append("\n");
        }
        return sb.toString();
    }

    private String csvValue(Object value) {
        if (value == null) return "";

        String text = value.toString();
        if (text.contains(",") || text.contains("\"") || text.contains("\n") || text.contains("\r")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }

        return text;
    }

    public Page<Run> getAllMyRuns(User user, Pageable pageable) {
        var userId = user.getId();
        return runRepository.findByUserId(userId, pageable);
    }
}
