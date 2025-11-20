package pokemon.runs.time.leaderboard.dto.auth;

public record RegisterResponseDTO(
        Long id,
        String username,
        String email,
        String role
) {
}

