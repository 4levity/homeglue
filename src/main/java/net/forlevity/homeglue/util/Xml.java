/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.util;

import lombok.extern.log4j.Log4j2;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/**
 * Object to handle XML document parsing and XPath query operations. Malformed XML and queries result in
 * empty results (and log entries), rather than exceptions.
 */
@Log4j2
public class Xml implements ErrorHandler {

    private final DocumentBuilderFactory xmlDocumentBuilderFactory = DocumentBuilderFactory.newInstance();
    private final XPathFactory xPathFactory = XPathFactory.newInstance();

    /**
     * Parse XML String.
     *
     * @param xml some XML text
     * @return DOM, or empty document if parsing failed
     */
    public Document parse(String xml) {
        byte[] bytes;
        try {
            bytes = xml.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new UnsupportedOperationException(e);
        }
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        return parse(stream);
    }

    /**
     * Parse XML InputStream.
     *
     * @param stream stream of XML text
     * @return DOM, or empty document if parsing failed
     */
    public Document parse(InputStream stream) {
        DocumentBuilder documentBuilder = makeDocumentBuilder();
        Document result = null;
        try {
            result = documentBuilder.parse(stream);
        } catch (SAXException | IOException e) {
            log.debug("failed to parse XML", e);
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

    private DocumentBuilder makeDocumentBuilder() {
        DocumentBuilder documentBuilder;
        try {
            documentBuilder = xmlDocumentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new UnsupportedOperationException(e);
        }
        documentBuilder.setErrorHandler(this);
        return documentBuilder;
    }

    @Override
    public void warning(SAXParseException exception) throws SAXException {
        log.warn("XML parse warning: {} {}", exception.getClass().getSimpleName(), exception.getMessage());
    }

    @Override
    public void error(SAXParseException exception) throws SAXException {
        log.warn("XML parse error: {} {}", exception.getClass().getSimpleName(), exception.getMessage());
    }

    @Override
    public void fatalError(SAXParseException exception) throws SAXException {
        log.warn("XML parse fatal error: {} {}", exception.getClass().getSimpleName(), exception.getMessage());
    }
}
