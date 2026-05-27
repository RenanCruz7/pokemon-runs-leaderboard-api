package pokemon.runs.time.leaderboard.infra.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import pokemon.runs.time.leaderboard.domain.user.User;
import pokemon.runs.time.leaderboard.repository.user.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SecurityFilter - Testes Unitários")
class SecurityFilterTest {

    @Mock
    private TokenService tokenService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SecurityFilter securityFilter;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Deve ignorar requisições sem header Authorization")
    void shouldIgnoreRequestWithoutAuthorizationHeader() throws Exception {
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();
        var filterChain = new MockFilterChain();

        securityFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(tokenService, userRepository);
    }

    @Test
    @DisplayName("Deve aceitar somente Authorization no formato Bearer token")
    void shouldIgnoreMalformedAuthorizationHeader() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic credentials");
        var response = new MockHttpServletResponse();
        var filterChain = new MockFilterChain();

        securityFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(tokenService, userRepository);
    }

    @Test
    @DisplayName("Deve autenticar usuário com token Bearer válido")
    void shouldAuthenticateUserWithValidBearerToken() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");
        var response = new MockHttpServletResponse();
        var filterChain = new MockFilterChain();
        var user = new User();

        when(tokenService.validateToken("valid-token")).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        securityFilter.doFilterInternal(request, response, filterChain);

        assertSame(user, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    @Test
    @DisplayName("Deve rejeitar token Bearer inválido sem consultar usuário")
    void shouldRejectInvalidBearerTokenWithoutUserLookup() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid-token");
        var response = new MockHttpServletResponse();
        var filterChain = new MockFilterChain();

        when(tokenService.validateToken("invalid-token")).thenReturn("");

        securityFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(userRepository, never()).findByUsername(org.mockito.ArgumentMatchers.anyString());
    }
}
