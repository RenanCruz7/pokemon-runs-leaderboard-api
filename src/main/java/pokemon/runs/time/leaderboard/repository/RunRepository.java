package pokemon.runs.time.leaderboard.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pokemon.runs.time.leaderboard.domain.run.Run;
import pokemon.runs.time.leaderboard.dto.AvgRunTimeByGameDTO;
import pokemon.runs.time.leaderboard.dto.RunsCountByGameDTO;

import java.time.Duration;
import java.util.List;

public interface RunRepository extends JpaRepository<Run, Long> {
    Page<Run> findByGameIgnoreCase(String game, Pageable pageable);

    @Query("SELECT r FROM runs r WHERE r.runTime <= :maxDuration")
    Page<Run> findFastestRuns(Duration maxDuration, Pageable pageable);

    @Query("SELECT r FROM runs r WHERE r.pokedexStatus >= :minStatus")
    Page<Run> findByMinPokedexStatus(int minStatus, Pageable pageable);

    @Query(
            value = "SELECT * FROM runs r WHERE r.pokemon_team ILIKE CONCAT('%', :pokemon, '%')",
            nativeQuery = true
    )
    Page<Run> findByPokemonInTeam(String pokemon, Pageable pageable);

    @Query("SELECT new pokemon.runs.time.leaderboard.dto.RunsCountByGameDTO(r.game, COUNT(r)) FROM runs r GROUP BY r.game")
    List<RunsCountByGameDTO> countRunsByGame();

    @Query("SELECT new pokemon.runs.time.leaderboard.dto.AvgRunTimeByGameDTO(r.game, AVG(r.runTime)) FROM runs r GROUP BY r.game")
    List<AvgRunTimeByGameDTO> avgRunTimeByGame();

    @Query(value = "SELECT pokemon, COUNT(*) as count FROM (SELECT unnest(string_to_array(pokemon_team, ',')) as pokemon FROM runs) as team GROUP BY pokemon ORDER BY count DESC LIMIT 10", nativeQuery = true)
    List<Object[]> topPokemonsUsed();


}
