/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.upnp;

import lombok.extern.log4j.Log4j2;
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

@Log4j2
public class Xml {

    private final DocumentBuilderFactory xmlDocumentBuilderFactory = DocumentBuilderFactory.newInstance();
    private final XPathFactory xPathFactory = XPathFactory.newInstance();

    /**
     * Parse XML.
     * @param xml some XML text
     * @return DOM, or null if parsing failed
     */
    public Document parse(String xml) {
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
     * Run an XPath query to find a specific node and return the text content.
     * @param doc DOM
     * @param query XPath query expression
     * @return text content of node, or null if query did not find one node
     */
    public String nodeText(Document doc, String query) {
        Node node = null;
        try {
            node = (Node) xPathFactory.newXPath().evaluate(query, doc, XPathConstants.NODE);
        } catch (XPathExpressionException e) {
            log.warn("failed to get text from node", e);
        }
        return node == null ? null : node.getTextContent();
    }
}
