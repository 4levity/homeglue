package net.forlevity.homeglue.device;

import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.apache.http.client.fluent.Request;
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

@Log4j2
@ToString(of = {"friendlyName","model","serialNumber","macAddress","firmwareVersion"})
public class WemoInsightConnector implements PowerMeterConnector {

    private final String hostAddress;
    private final DocumentBuilderFactory xmlDocumentBuilderFactory;
    private final XPathFactory xPathFactory;
    private int port = -1;
    private String friendlyName;
    private String model;
    private String serialNumber;
    private String macAddress;
    private String firmwareVersion;

    public WemoInsightConnector(String hostAddress) {
        this.hostAddress = hostAddress;
        xmlDocumentBuilderFactory = DocumentBuilderFactory.newInstance();
        xPathFactory = XPathFactory.newInstance();
    }

    @Override
    public boolean connect() {
        log.info("trying to connect to wemo at address: {} ...", hostAddress);
        for (int tryPort = 49153; tryPort <= 49155; tryPort++ ) {
            String baseUrl = String.format("http://%s:%d/", hostAddress, tryPort);
            String result = null;
            try {
                result = Request.Get(baseUrl + "/setup.xml").execute().returnContent().asString();
                log.debug("setup.xml contents: {}", result);
                parseWemoSetup(result);
                log.info("contacted {} : {}", baseUrl, this.toString());
                port = tryPort;
                return true;
            } catch (IOException e) {
                log.warn("failed to connect to {} : {} {}", baseUrl, e.getClass().getSimpleName(), e.getMessage());
            } catch (ParserConfigurationException | SAXException e) {
                log.warn("failed to parse setup.xml", e);
            }
        }
        port = -1;
        return false;
    }

    @Override
    public boolean isConnected() {
        return port > 0;
    }

    private void parseWemoSetup(String setupXml) throws IOException, SAXException, ParserConfigurationException {
        DocumentBuilder db = xmlDocumentBuilderFactory.newDocumentBuilder();
        Document doc = db.parse(new ByteArrayInputStream(setupXml.getBytes("utf-8")));
        try {
            this.friendlyName = nodeText(doc, "/root/device/friendlyName");
            this.model = nodeText(doc, "/root/device/modelDescription");
            this.serialNumber = nodeText(doc, "/root/device/serialNumber");
            this.macAddress = nodeText(doc, "/root/device/macAddress");
            this.firmwareVersion = nodeText(doc, "/root/device/firmwareVersion");
        } catch (XPathExpressionException e) {
            log.error(e);
        }
    }

    private String nodeText(Document doc, String query) throws XPathExpressionException {
        Node node = (Node) xPathFactory.newXPath().evaluate(query, doc, XPathConstants.NODE);
        return node.getTextContent();
    }

    @Override
    public PowerMeterData read() {
        String url = String.format("http://%s:%d/upnp/control/insight1", hostAddress, port);
        String payload =
            "<?xml version=\"1.0\" encoding=utf-8\"?>\n" +
            "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
            "    <s:Body>\n" +
            "        <u:GetInsightParams xmlns:u=\"urn:Belkin:service:insight:1\">\n" +
            "        </u:GetInsightParams>\n" +
            "    </s:Body>\n" +
            "</s:Envelope>";
        String result = null;
        try {
            result = Request.Post(url)
                    .setHeader("SOAPAction","\"urn:Belkin:service:insight:1#GetInsightParams\"")
                    .bodyString(payload, ContentType.TEXT_XML)
                    .execute().returnContent().asString();
            DocumentBuilder db = xmlDocumentBuilderFactory.newDocumentBuilder();
            Document doc = db.parse(new ByteArrayInputStream(result.getBytes("utf-8")));
            Node node = (Node) xPathFactory.newXPath().evaluate("//InsightParams", doc, XPathConstants.NODE);
            String insightParams = node.getTextContent();
            String[] params = insightParams.split("\\|");
            double milliwatts = Double.valueOf(params[7]);
            log.debug("p = {} / mw = {}", insightParams, params[7], milliwatts);
            return new PowerMeterData(milliwatts / 1000.0);
        } catch (IOException e) {
            log.warn("failed to read wemo meter", e);
        } catch (XPathExpressionException | ParserConfigurationException | SAXException e) {
            log.warn("failed to parse SOAP response", e);
        }
        return null;
    }
}
