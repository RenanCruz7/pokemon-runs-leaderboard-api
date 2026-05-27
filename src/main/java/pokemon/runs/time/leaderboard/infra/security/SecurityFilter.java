package pokemon.runs.time.leaderboard.infra.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import pokemon.runs.time.leaderboard.repository.user.UserRepository;

import java.io.IOException;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        var token = this.recoverToken(request);
        if(token != null) {
            var login = tokenService.validateToken(token);
            if(login == null || login.isBlank()) {
                filterChain.doFilter(request, response);
                return;
            }

            var user = userRepository.findByUsername(login);
            if(user.isPresent()) {
                var userDetails = user.get();
                var authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        filterChain.doFilter(request, response);
    }

    private String recoverToken(HttpServletRequest request) {
        var authHeader = request.getHeader("Authorization");
        if(authHeader == null || authHeader.isBlank() || !authHeader.startsWith(BEARER_PREFIX)) return null;

        var token = authHeader.substring(BEARER_PREFIX.length()).trim();
        if(token.isBlank()) return null;

        return token;
    }
}

