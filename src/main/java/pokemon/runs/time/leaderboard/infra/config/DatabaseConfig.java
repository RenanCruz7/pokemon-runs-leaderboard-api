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
        String databaseUrl = System.getenv("DATABASE_URL");

        // Se DATABASE_URL existe (Render), converte para formato JDBC
        if (databaseUrl != null && databaseUrl.startsWith("postgresql://")) {
            databaseUrl = "jdbc:" + databaseUrl;
        }

        // Se não tem DATABASE_URL, usa SPRING_DATASOURCE_URL
        if (databaseUrl == null) {
            databaseUrl = System.getenv("SPRING_DATASOURCE_URL");
        }

        // Fallback para desenvolvimento local
        if (databaseUrl == null) {
            databaseUrl = "jdbc:postgresql://localhost:5432/leaderboard_db";
        }

        String username = System.getenv("SPRING_DATASOURCE_USERNAME");
        if (username == null) username = "postgres";

        String password = System.getenv("SPRING_DATASOURCE_PASSWORD");
        if (password == null) password = "";

        return DataSourceBuilder
                .create()
                .url(databaseUrl)
                .username(username)
                .password(password)
                .driverClassName("org.postgresql.Driver")
                .build();
    }
}

