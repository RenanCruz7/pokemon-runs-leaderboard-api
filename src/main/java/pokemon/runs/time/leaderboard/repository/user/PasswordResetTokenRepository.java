package pokemon.runs.time.leaderboard.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pokemon.runs.time.leaderboard.domain.user.PasswordResetToken;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    void deleteByExpiryDateBefore(LocalDateTime now);

    void deleteByUserId(Long userId);
}

