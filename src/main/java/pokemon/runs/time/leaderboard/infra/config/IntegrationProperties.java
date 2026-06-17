package pokemon.runs.time.leaderboard.infra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "integration")
public class IntegrationProperties {

    private Duration connectTimeout = Duration.ofSeconds(3);
    private Duration readTimeout = Duration.ofSeconds(5);
    private Rest rest = new Rest();
    private Soap soap = new Soap();

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Duration getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
    }

    public Rest getRest() {
        return rest;
    }

    public void setRest(Rest rest) {
        this.rest = rest;
    }

    public Soap getSoap() {
        return soap;
    }

    public void setSoap(Soap soap) {
        this.soap = soap;
    }

    public static class Rest {

        private String pokeApiBaseUrl = "https://pokeapi.co/api/v2";

        public String getPokeApiBaseUrl() {
            return pokeApiBaseUrl;
        }

        public void setPokeApiBaseUrl(String pokeApiBaseUrl) {
            this.pokeApiBaseUrl = pokeApiBaseUrl;
        }
    }

    public static class Soap {

        private String numberConversionUrl = "https://www.dataaccess.com/webservicesserver/NumberConversion.wso";

        public String getNumberConversionUrl() {
            return numberConversionUrl;
        }

        public void setNumberConversionUrl(String numberConversionUrl) {
            this.numberConversionUrl = numberConversionUrl;
        }
    }
}
