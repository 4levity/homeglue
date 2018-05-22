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
import net.forlevity.homeglue.upnp.Xml;
import org.apache.http.entity.ContentType;
import org.w3c.dom.Document;

import java.io.IOException;
import java.util.Map;

/**
 * Base class to assist in building a device connector that issues SOAP requests to its device and parses XML results.
 */
@Log4j2
public abstract class AbstractSoapDeviceConnector extends AbstractDeviceConnector {

    @Getter(AccessLevel.PROTECTED)
    private final SimpleHttpClient httpClient;

    protected final Xml xml = new Xml();

    protected AbstractSoapDeviceConnector(SimpleHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Execute a SOAP-ish request to a device.
     *
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
            document = xml.parse(result);
        } catch (IOException e) {
            log.warn("failed to execute SOAP request: {} {}", e.getClass().getSimpleName(), e.getMessage());
        }
        return document;
    }
}
