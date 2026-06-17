package pokemon.runs.time.leaderboard.infra.config;

import jakarta.xml.soap.MessageFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.transport.http.HttpUrlConnectionMessageSender;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;

@Configuration
@EnableConfigurationProperties(IntegrationProperties.class)
public class IntegrationClientConfig {

    @Bean
    public ClientHttpRequestFactory integrationClientHttpRequestFactory(IntegrationProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) properties.getConnectTimeout().toMillis());
        factory.setReadTimeout((int) properties.getReadTimeout().toMillis());
        return factory;
    }

    @Bean
    public RestClient pokeApiRestClient(ClientHttpRequestFactory integrationClientHttpRequestFactory,
                                        IntegrationProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.getRest().getPokeApiBaseUrl())
                .requestFactory(integrationClientHttpRequestFactory)
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Bean
    public WebServiceTemplate numberConversionWebServiceTemplate(IntegrationProperties properties) throws Exception {
        SaajSoapMessageFactory messageFactory = new SaajSoapMessageFactory(MessageFactory.newInstance());
        messageFactory.afterPropertiesSet();

        HttpUrlConnectionMessageSender messageSender = new HttpUrlConnectionMessageSender();
        messageSender.setConnectionTimeout(properties.getConnectTimeout());
        messageSender.setReadTimeout(properties.getReadTimeout());

        WebServiceTemplate webServiceTemplate = new WebServiceTemplate(messageFactory);
        webServiceTemplate.setDefaultUri(properties.getSoap().getNumberConversionUrl());
        webServiceTemplate.setMessageSender(messageSender);
        return webServiceTemplate;
    }
}
