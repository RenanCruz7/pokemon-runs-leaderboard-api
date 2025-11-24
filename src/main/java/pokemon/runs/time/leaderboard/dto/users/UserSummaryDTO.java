package pokemon.runs.time.leaderboard.dto.users;

import pokemon.runs.time.leaderboard.domain.user.User;

public record UserSummaryDTO(
        Long id,
        String username,
        String email
) {
    public UserSummaryDTO(User user) {
        this(user.getId(), user.getUsername(), user.getEmail());
    }
}

