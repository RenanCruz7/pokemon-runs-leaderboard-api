package pokemon.runs.time.leaderboard.infra.errors;

import org.springframework.http.HttpStatus;

public class ExternalServiceException extends RuntimeException {

    private final HttpStatus status;
    private final String serviceName;

    public ExternalServiceException(HttpStatus status, String serviceName, String message) {
        super(message);
        this.status = status;
        this.serviceName = serviceName;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getServiceName() {
        return serviceName;
    }
}
