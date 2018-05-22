package net.forlevity.homeglue.device;

import com.google.common.collect.ImmutableMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.forlevity.homeglue.http.SimpleHttpClient;
import org.apache.http.entity.ContentType;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

@Log4j2
public abstract class AbstractSoapDeviceConnector extends AbstractDeviceConnector {

    @Getter(AccessLevel.PROTECTED)
    private final SimpleHttpClient httpClient;
    private final DocumentBuilderFactory xmlDocumentBuilderFactory = DocumentBuilderFactory.newInstance();
    private final XPathFactory xPathFactory = XPathFactory.newInstance();

    protected AbstractSoapDeviceConnector(SimpleHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    protected Document parse(String setupXml) {
        DocumentBuilder documentBuilder;
        byte[] bytes;
        try {
            bytes = setupXml.getBytes("utf-8");
            documentBuilder = xmlDocumentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException | UnsupportedEncodingException e) {
            throw new UnsupportedOperationException(e);
        }
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        Document result = null;
        try {
            result = documentBuilder.parse(stream);
        } catch (SAXException | IOException e) {
            log.warn("failed to parse XML");
        }
        return result == null ? documentBuilder.newDocument() : result;
    }


    protected Document execSoapRequest(String url, String urn, String action) {
        String payload = String.format("<?xml version=\"1.0\" encoding=utf-8\"?>" +
                "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">" +
                    "<s:Body>" +
                        "<u:%s xmlns:u=\"%s\"/>" +
                    "</s:Body>" +
                "</s:Envelope>", action, urn);

        Document document = null;
        try {
            Map<String, String> extraHeaders = ImmutableMap.of("SOAPAction", String.format("\"%s#%s\"",urn,action));
            String result = httpClient.post(url, extraHeaders, payload, ContentType.TEXT_XML);
            DocumentBuilder db = xmlDocumentBuilderFactory.newDocumentBuilder();
            document = db.parse(new ByteArrayInputStream(result.getBytes("utf-8")));
        } catch (IOException e) {
            log.warn("failed to execute SOAP request", e);
        } catch (ParserConfigurationException | SAXException e) {
            log.warn("failed to parse XML from device", e);
        }
        return document;
    }

    protected String nodeText(Document doc, String query) {
        Node node = null;
        try {
            node = (Node) xPathFactory.newXPath().evaluate(query, doc, XPathConstants.NODE);
        } catch (XPathExpressionException e) {
            log.warn("failed to get text from node", e);
        }
        return node == null ? null : node.getTextContent();
    }

}
