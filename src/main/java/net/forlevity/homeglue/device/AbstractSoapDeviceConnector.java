/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

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

/**
 * Base class to assist in building a device connector that issues SOAP requests to its device and parses XML results.
 */
@Log4j2
public abstract class AbstractSoapDeviceConnector extends AbstractDeviceConnector {

    @Getter(AccessLevel.PROTECTED)
    private final SimpleHttpClient httpClient;
    private final DocumentBuilderFactory xmlDocumentBuilderFactory = DocumentBuilderFactory.newInstance();
    private final XPathFactory xPathFactory = XPathFactory.newInstance();

    protected AbstractSoapDeviceConnector(SimpleHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Convenience method to parse XML.
     * @param xml some XML text
     * @return DOM, or null if parsing failed
     */
    protected Document parse(String xml) {
        DocumentBuilder documentBuilder;
        byte[] bytes;
        try {
            bytes = xml.getBytes("utf-8");
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

    /**
     * Convenience method to execute a SOAP-ish request to a device.
     * @param url http endpoint URL
     * @param urn SOAP URN
     * @param action SOAP action
     * @return DOM or null if request failed
     */
    protected Document execSoapRequest(String url, String urn, String action) {
        String payload = String.format("<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
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
            log.warn("failed to execute SOAP request: {} {}", e.getClass().getSimpleName(), e.getMessage());
        } catch (ParserConfigurationException | SAXException e) {
            log.warn("failed to parse XML from device", e);
        }
        return document;
    }

    /**
     * Convenience method to run an XPath query to find a specific node and return the text content.
     * @param doc DOM
     * @param query XPath query expression
     * @return text content of node, or null if query did not find one node
     */
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
