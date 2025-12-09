package pokemon.runs.time.leaderboard.infra.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Permitir todas as origens (para desenvolvimento)
        // Em produção, substitua por: configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "https://seu-dominio.com"));
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));

        // Permitir todos os métodos HTTP
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Permitir todos os headers
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // Permitir credenciais (cookies, authorization headers)
        configuration.setAllowCredentials(true);

        // Expor headers na resposta
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Location"));

        // Tempo de cache da configuração de CORS (1 hora)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}

