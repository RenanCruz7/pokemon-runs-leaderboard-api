package pokemon.runs.time.leaderboard.dto.users;

import pokemon.runs.time.leaderboard.domain.user.User;
import pokemon.runs.time.leaderboard.dto.runs.DetailsRunDTO;

import java.util.List;
import java.util.stream.Collectors;

public record UserDetailsDTO(
        Long id,
        String username,
        String email,
        String role,
        List<DetailsRunDTO> runs
) {
    public UserDetailsDTO(User user) {
        this(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getRuns() != null ? user.getRuns().stream()
                        .map(DetailsRunDTO::new)
                        .collect(Collectors.toList()) : List.of()
        );
    }
}

