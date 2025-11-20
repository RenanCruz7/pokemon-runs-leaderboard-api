package pokemon.runs.time.leaderboard.dto.auth;

public record LoginResponseDTO(
        String token,
        Long id,
        String username,
        String email,
        String role
) {
}

