package pokemon.runs.time.leaderboard.infra.errors;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}

