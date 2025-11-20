package pokemon.runs.time.leaderboard.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;
import pokemon.runs.time.leaderboard.domain.user.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByRole(String role);

    Optional<User> findByUsername(String username);
}
