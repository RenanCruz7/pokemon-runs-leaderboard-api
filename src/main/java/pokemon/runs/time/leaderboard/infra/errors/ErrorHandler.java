package pokemon.runs.time.leaderboard.infra.errors;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(ErrorHandler.class);

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handle404Error(EntityNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(404).body(new ErrorResponse("Recurso não encontrado", ex.getMessage()));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(404).body(new ErrorResponse("Recurso não encontrado", ex.getMessage()));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handle405Error(HttpRequestMethodNotSupportedException ex) {
        log.warn("Method not allowed: {}", ex.getMessage());
        return ResponseEntity.status(405).body(new ErrorResponse("Método não permitido", ex.getMethod() + " não é suportado para esta rota"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleBadRequestError(MethodArgumentNotValidException ex) {
        var errors = ex.getFieldErrors();
        log.warn("Validation error: {} field(s) with errors", errors.size());
        return ResponseEntity.badRequest().body(errors.stream().map(ValidationErrorData::new).toList());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        log.warn("Authentication failed: bad credentials");
        return ResponseEntity.status(401).body(new ErrorResponse("Credenciais inválidas", "Usuário ou senha incorretos"));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex) {
        log.warn("Unauthorized access: {}", ex.getMessage());
        return ResponseEntity.status(403).body(new ErrorResponse("Não autorizado", ex.getMessage()));
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResource(DuplicateResourceException ex) {
        log.warn("Duplicate resource: {}", ex.getMessage());
        return ResponseEntity.status(409).body(new ErrorResponse("Recurso duplicado", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Invalid argument: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(new ErrorResponse("Argumento inválido", ex.getMessage()));
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAuthorizationDenied(AuthorizationDeniedException ex) {
        log.warn("Authorization denied: {}", ex.getMessage());
        return ResponseEntity.status(403).body(new ErrorResponse("Acesso negado", "Você não tem permissão para acessar este recurso"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericError(Exception ex) {
        log.error("Internal server error", ex);
        return ResponseEntity.status(500).body(new ErrorResponse("Erro interno do servidor", "Ocorreu um erro inesperado. Por favor, tente novamente mais tarde."));
    }

    private record ValidationErrorData(String campo, String mensagem) {
        public ValidationErrorData(FieldError error) {
            this(error.getField(), error.getDefaultMessage());
        }
    }

    private record ErrorResponse(String erro, String detalhes) {}
}
