package pokemon.runs.time.leaderboard.dto.users;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordDTO(
        @NotBlank(message = "Token é obrigatório")
        String token,

        @NotBlank(message = "Nova senha é obrigatória")
        @Size(min = 6, message = "Nova senha deve ter no mínimo 6 caracteres")
        String newPassword
) {
}

