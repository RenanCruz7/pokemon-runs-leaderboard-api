package pokemon.runs.time.leaderboard.integration.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.ws.client.WebServiceTransportException;
import org.springframework.ws.client.WebServiceIOException;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.client.SoapFaultClientException;
import org.springframework.xml.transform.StringSource;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import pokemon.runs.time.leaderboard.integration.dto.NumberToWordsRequest;
import pokemon.runs.time.leaderboard.integration.dto.NumberToWordsResult;
import pokemon.runs.time.leaderboard.infra.errors.ExternalServiceException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.SocketTimeoutException;

@Component
public class NumberConversionSoapClient {

    private static final Logger log = LoggerFactory.getLogger(NumberConversionSoapClient.class);
    private static final String SERVICE_NAME = "NumberConversionSOAP";

    @Autowired
    private WebServiceTemplate numberConversionWebServiceTemplate;

    public NumberToWordsResult convert(NumberToWordsRequest request) {
        String payload = """
                <NumberToWords xmlns=\"http://www.dataaccess.com/webservicesserver/\">
                    <ubiNum>%d</ubiNum>
                </NumberToWords>
                """.formatted(request.number());

        try {
            StringWriter writer = new StringWriter();
            numberConversionWebServiceTemplate.sendSourceAndReceiveToResult(
                    new StringSource(payload),
                    new StreamResult(writer)
            );

            String words = extractNumberInWords(writer.toString());
            return new NumberToWordsResult(request.number(), words);
        } catch (SoapFaultClientException ex) {
            log.warn("SOAP fault ao consumir {}: {}", SERVICE_NAME, ex.getMessage());
            throw new ExternalServiceException(HttpStatus.BAD_GATEWAY, SERVICE_NAME, "Servico SOAP externo retornou fault");
        } catch (WebServiceIOException ex) {
            throw mapWebServiceException(ex);
        } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException ex) {
            log.warn("Resposta SOAP invalida recebida de {}: {}", SERVICE_NAME, ex.getMessage());
            throw new ExternalServiceException(HttpStatus.BAD_GATEWAY, SERVICE_NAME, "Resposta invalida recebida do servico SOAP externo");
        }
    }

    private String extractNumberInWords(String responseXml)
            throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

        Document document = factory.newDocumentBuilder().parse(new InputSource(new StringReader(responseXml)));
        String words = (String) XPathFactory.newInstance()
                .newXPath()
                .evaluate("//*[local-name()='NumberToWordsResult']/text()", document, XPathConstants.STRING);

        if (words == null || words.trim().isEmpty()) {
            log.warn("Resposta SOAP sem NumberToWordsResult recebida de {}", SERVICE_NAME);
            throw new ExternalServiceException(HttpStatus.BAD_GATEWAY, SERVICE_NAME, "Resposta invalida recebida do servico SOAP externo");
        }

        return words.trim();
    }

    private ExternalServiceException mapWebServiceException(Exception ex) {
        Throwable cause = ex.getCause();
        if (cause instanceof SocketTimeoutException) {
            log.warn("Timeout ao consumir {}: {}", SERVICE_NAME, ex.getMessage());
            return new ExternalServiceException(HttpStatus.GATEWAY_TIMEOUT, SERVICE_NAME, "Timeout ao consumir servico SOAP externo");
        }

        log.warn("Falha de comunicacao ao consumir {}: {}", SERVICE_NAME, ex.getMessage());
        return new ExternalServiceException(HttpStatus.BAD_GATEWAY, SERVICE_NAME, "Falha ao consumir servico SOAP externo");
    }
}
