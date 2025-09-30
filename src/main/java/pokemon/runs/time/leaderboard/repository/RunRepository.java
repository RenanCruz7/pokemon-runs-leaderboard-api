package pokemon.runs.time.leaderboard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pokemon.runs.time.leaderboard.domain.Run;

public interface RunRepository extends JpaRepository<Run, Long> {
}
