package pokemon.runs.time.leaderboard.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pokemon.runs.time.leaderboard.domain.Run;

import java.time.Duration;

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



}
