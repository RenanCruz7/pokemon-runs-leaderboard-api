package pokemon.runs.time.leaderboard.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.docker.compose.enabled=false",
        "integration.http.connect-timeout=200ms",
        "integration.http.read-timeout=200ms"
})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@DisplayName("IntegrationController - Integracoes REST e SOAP")
class IntegrationControllerIntegrationTest {

    private static final StubHttpServer restServer = new StubHttpServer("/pokemon/pikachu");
    private static final StubHttpServer soapServer = new StubHttpServer("/number-conversion");

    @Autowired
    private MockMvc mockMvc;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("integration.rest.poke-api-base-url", restServer::getBaseUrl);
        registry.add("integration.soap.number-conversion-url", soapServer::getUrl);
    }

    @BeforeEach
    void setUp() {
        restServer.respond(200, "application/json", """
                {
                  "id": 25,
                  "name": "pikachu",
                  "base_experience": 112,
                  "types": [
                    { "slot": 1, "type": { "name": "electric", "url": "https://pokeapi.co/api/v2/type/13/" } }
                  ]
                }
                """);

        soapServer.respond(200, "text/xml;charset=UTF-8", """
                <?xml version="1.0" encoding="utf-8"?>
                <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                  <soap:Body>
                    <NumberToWordsResponse xmlns="http://www.dataaccess.com/webservicesserver/">
                      <NumberToWordsResult>twenty five </NumberToWordsResult>
                    </NumberToWordsResponse>
                  </soap:Body>
                </soap:Envelope>
                """);
    }

    @AfterAll
    static void tearDown() {
        restServer.stop();
        soapServer.stop();
    }

    @Test
    @DisplayName("GET /integrations/pokemon/{pokemon} combina dados REST e SOAP com sucesso")
    void getPokemonSummarySuccess() throws Exception {
        mockMvc.perform(get("/integrations/pokemon/pikachu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pokemon").value("pikachu"))
                .andExpect(jsonPath("$.pokedexNumber").value(25))
                .andExpect(jsonPath("$.pokedexNumberInWords").value("twenty five"))
                .andExpect(jsonPath("$.baseExperience").value(112))
                .andExpect(jsonPath("$.types", hasSize(1)))
                .andExpect(jsonPath("$.types[0]").value("electric"));
    }

    @Test
    @DisplayName("GET /integrations/pokemon/{pokemon} retorna 502 quando integracao REST fica indisponivel")
    void getPokemonSummaryReturnsBadGatewayWhenRestServiceIsUnavailable() throws Exception {
        restServer.respond(503, "application/json", "{\"error\":\"service unavailable\"}");

        mockMvc.perform(get("/integrations/pokemon/pikachu"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.erro").value("Falha em integracao externa"))
                .andExpect(jsonPath("$.detalhes").value("Servico REST externo retornou status 503"));
    }

    @Test
    @DisplayName("GET /integrations/pokemon/{pokemon} retorna 502 quando integracao REST responde payload invalido")
    void getPokemonSummaryReturnsBadGatewayWhenRestPayloadIsInvalid() throws Exception {
        restServer.respond(200, "application/json", "{\"name\":\"pikachu\"}");

        mockMvc.perform(get("/integrations/pokemon/pikachu"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.erro").value("Falha em integracao externa"))
                .andExpect(jsonPath("$.detalhes").value("Resposta invalida recebida do servico REST externo"));
    }

    @Test
    @DisplayName("GET /integrations/pokemon/{pokemon} retorna 502 quando integracao SOAP fica indisponivel")
    void getPokemonSummaryReturnsBadGatewayWhenSoapServiceIsUnavailable() throws Exception {
        soapServer.respond(503, "text/plain;charset=UTF-8", "service unavailable");

        mockMvc.perform(get("/integrations/pokemon/pikachu"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.erro").value("Falha em integracao externa"))
                .andExpect(jsonPath("$.detalhes").value("Falha ao consumir servico SOAP externo"));
    }

    @Test
    @DisplayName("GET /integrations/pokemon/{pokemon} retorna 502 quando integracao SOAP responde XML invalido")
    void getPokemonSummaryReturnsBadGatewayWhenSoapPayloadIsInvalid() throws Exception {
        soapServer.respond(200, "text/xml;charset=UTF-8", """
                <?xml version="1.0" encoding="utf-8"?>
                <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                  <soap:Body>
                    <NumberToWordsResponse xmlns="http://www.dataaccess.com/webservicesserver/"/>
                  </soap:Body>
                </soap:Envelope>
                """);

        mockMvc.perform(get("/integrations/pokemon/pikachu"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.erro").value("Falha em integracao externa"))
                .andExpect(jsonPath("$.detalhes").value("Resposta invalida recebida do servico SOAP externo"));
    }

    private static class StubHttpServer {

        private final HttpServer server;
        private final String path;
        private final AtomicReference<StubResponse> responseRef = new AtomicReference<>();

        private StubHttpServer(String path) {
            this.path = path;
            try {
                this.server = HttpServer.create(new InetSocketAddress(0), 0);
            } catch (IOException ex) {
                throw new IllegalStateException("Nao foi possivel iniciar servidor stub", ex);
            }

            this.server.createContext(path, new DelegatingHandler(responseRef));
            this.server.setExecutor(Executors.newCachedThreadPool());
            this.server.start();
        }

        private void respond(int status, String contentType, String body) {
            responseRef.set(new StubResponse(status, contentType, body, 0));
        }

        private void respondWithDelay(int status, String contentType, String body, long delayMillis) {
            responseRef.set(new StubResponse(status, contentType, body, delayMillis));
        }

        private String getBaseUrl() {
            return "http://localhost:" + server.getAddress().getPort();
        }

        private String getUrl() {
            return getBaseUrl() + path;
        }

        private void stop() {
            server.stop(0);
        }
    }

    private record StubResponse(int status, String contentType, String body, long delayMillis) {
    }

    private static class DelegatingHandler implements HttpHandler {

        private final AtomicReference<StubResponse> responseRef;

        private DelegatingHandler(AtomicReference<StubResponse> responseRef) {
            this.responseRef = responseRef;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            StubResponse response = responseRef.get();
            if (response == null) {
                exchange.sendResponseHeaders(500, -1);
                exchange.close();
                return;
            }

            if (response.delayMillis() > 0) {
                try {
                    Thread.sleep(response.delayMillis());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }

            byte[] body = response.body().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", response.contentType());
            exchange.sendResponseHeaders(response.status(), body.length);

            try (OutputStream outputStream = exchange.getResponseBody()) {
                outputStream.write(body);
            }
        }
    }
}
