package pokemon.runs.time.leaderboard.dto.users;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RequestPasswordResetDTO(
        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email deve ser válido")
        String email
) {
}

