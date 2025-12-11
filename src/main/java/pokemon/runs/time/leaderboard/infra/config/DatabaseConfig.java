package pokemon.runs.time.leaderboard.infra.config;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {

    @Bean
    @Primary
    public DataSource dataSource() {
        // Render (e outros provedores PaaS) fornecem as credenciais separadamente.
        // Esta é a forma mais robusta de configurar a conexão.
        String dbHost = System.getenv("DB_HOST");
        String dbPort = System.getenv("DB_PORT");
        String dbName = System.getenv("DB_NAME");
        String username = System.getenv("DB_USER");
        String password = System.getenv("DB_PASS");

        String jdbcUrl;

        // Se as variáveis específicas do Render estiverem presentes, use-as.
        if (dbHost != null && dbPort != null && dbName != null && username != null) {
            jdbcUrl = String.format("jdbc:postgresql://%s:%s/%s", dbHost, dbPort, dbName);
        } else {
            // Fallback para desenvolvimento local se as variáveis do Render não estiverem definidas.
            jdbcUrl = System.getenv("SPRING_DATASOURCE_URL");
            if (jdbcUrl == null) {
                jdbcUrl = "jdbc:postgresql://localhost:5432/leaderboard_db";
            }
            if (username == null) {
                username = System.getenv("SPRING_DATASOURCE_USERNAME");
                if (username == null) username = "postgres";
            }
            if (password == null) {
                password = System.getenv("SPRING_DATASOURCE_PASSWORD");
                if (password == null) password = "";
            }
        }

        return DataSourceBuilder
                .create()
                .url(jdbcUrl)
                .username(username)
                .password(password)
                .driverClassName("org.postgresql.Driver")
                .build();
    }
}

