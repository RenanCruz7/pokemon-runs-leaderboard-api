package pokemon.runs.time.leaderboard.infra.config;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

@Configuration
public class DatabaseConfig {

    @Bean
    @Primary
    public DataSource dataSource() {
        String databaseUrl = System.getenv("DATABASE_URL");
        String username = System.getenv("SPRING_DATASOURCE_USERNAME");
        String password = System.getenv("SPRING_DATASOURCE_PASSWORD");

        // Se DATABASE_URL existe (Render), precisa fazer parse correto
        if (databaseUrl != null && databaseUrl.startsWith("postgres://")) {
            try {
                URI dbUri = new URI(databaseUrl);

                // Extrair username e password da URI
                String userInfo = dbUri.getUserInfo();
                if (userInfo != null) {
                    String[] credentials = userInfo.split(":", 2);
                    username = credentials[0];
                    if (credentials.length > 1) {
                        password = credentials[1];
                    }
                }

                // Reconstruir a URL no formato JDBC
                String host = dbUri.getHost();
                int port = dbUri.getPort();
                String path = dbUri.getPath();

                databaseUrl = String.format("jdbc:postgresql://%s:%d%s", host, port, path);

            } catch (URISyntaxException e) {
                throw new RuntimeException("Failed to parse DATABASE_URL", e);
            }
        }
        // Se DATABASE_URL não existe, usa SPRING_DATASOURCE_URL
        else if (databaseUrl == null) {
            databaseUrl = System.getenv("SPRING_DATASOURCE_URL");
        }

        // Fallback para desenvolvimento local
        if (databaseUrl == null) {
            databaseUrl = "jdbc:postgresql://localhost:5432/leaderboard_db";
        }

        if (username == null) username = "postgres";
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

