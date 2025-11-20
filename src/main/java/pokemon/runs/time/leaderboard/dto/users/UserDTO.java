package pokemon.runs.time.leaderboard.dto.users;

public record UserDTO(
        Long id,
        String username,
        String email,
        String role
) {
}

