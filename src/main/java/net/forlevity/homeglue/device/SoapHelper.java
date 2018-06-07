/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.device;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.forlevity.homeglue.http.SimpleHttpClient;
import net.forlevity.homeglue.util.Xml;
import org.apache.http.entity.ContentType;
import org.w3c.dom.Document;

import java.io.IOException;
import java.util.Map;

/**
 * Helper for issuing SOAP requests and parsing XML results.
 */
@Log4j2
public class SoapHelper {

    @Getter
    private final SimpleHttpClient httpClient;

    @Getter
    private final Xml xml = new Xml();

    @Inject
    public SoapHelper(SimpleHttpClient httpClient) {
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
    public Document execSoapRequest(String url, String urn, String action, String params) {
        String payload = String.format("<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
                "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">" +
                    "<s:Body>" +
                        "<u:%s xmlns:u=\"%s\">%s</u:%s>" +
                    "</s:Body>" +
                "</s:Envelope>", action, urn, params, action);

        Document document = null;
        try {
            Map<String, String> extraHeaders = ImmutableMap.of("SOAPAction", String.format("\"%s#%s\"",urn,action));
            String result = httpClient.post(url, extraHeaders, payload, ContentType.TEXT_XML);
            document = xml.parse(result);
        } catch (IOException e) {
            log.info("SOAP request failed: {} {}", e.getClass().getSimpleName(), e.getMessage());
        }
        return document;
    }
}
