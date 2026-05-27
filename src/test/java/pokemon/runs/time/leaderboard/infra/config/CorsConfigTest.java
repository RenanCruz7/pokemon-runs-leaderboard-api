package pokemon.runs.time.leaderboard.infra.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("CorsConfig - Testes Unitários")
class CorsConfigTest {

    @Test
    @DisplayName("Deve usar apenas origens configuradas e ignorar espaços")
    void shouldUseConfiguredOrigins() {
        var corsConfig = new CorsConfig();
        ReflectionTestUtils.setField(corsConfig, "allowedOrigins", " http://localhost:3000 , https://app.example.com ");

        var source = corsConfig.corsConfigurationSource();
        var configuration = source.getCorsConfiguration(new MockHttpServletRequest());

        assertEquals(List.of("http://localhost:3000", "https://app.example.com"), configuration.getAllowedOriginPatterns());
    }
}
