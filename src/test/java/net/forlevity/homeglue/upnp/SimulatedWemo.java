/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.upnp;

import io.resourcepool.ssdp.model.DiscoveryRequest;
import io.resourcepool.ssdp.model.SsdpService;
import net.forlevity.homeglue.HomeglueTests;
import net.forlevity.homeglue.http.SimpleHttpClient;
import org.apache.http.entity.ContentType;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;
import java.util.function.Consumer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SimulatedWemo implements SsdpSearcher, SimpleHttpClient {

    private static final String ERROR_RESPONSE = "error";

    private final InetAddress inetAddress;
    private final String setupXml;
    private final Document setupDocument;
    private final String location;

    public SimulatedWemo(InetAddress inetAddress, int port, String setupXmlName) {
        this.inetAddress = inetAddress;
        this.setupXml = HomeglueTests.resourceAsString(setupXmlName);
        this.setupDocument = null;//parse(setupXml);
        this.location = String.format("http://%s:%d/setup.xml", inetAddress.getHostAddress(), port);
    }

    @Override
    public String get(String url) throws IOException {
        if (url.endsWith("setup.xml")) {
            return setupXml;
        }
        return ERROR_RESPONSE;
    }

    @Override
    public String post(String url, Map<String, String> headers, String payload, ContentType contentType) throws IOException {
        if (url.endsWith("/upnp/control/insight1")
                && payload.equals(HomeglueTests.resourceAsString("insightparams_request.xml"))
                && headers.get("SOAPAction").equals("urn:Belkin:service:insight:1#GetInsightParams")
                && contentType.equals(ContentType.TEXT_XML) ) {
            return HomeglueTests.resourceAsString("insightparams_response.xml");
        }
        return ERROR_RESPONSE;
    }

    @Override
    public BackgroundProcessHandle startDiscovery(DiscoveryRequest discoveryRequest, Consumer<SsdpService> serviceConsumer) {
        if (discoveryRequest.getServiceTypes().contains("upnp:rootdevice")) {
            // non-compliant WeMo only answers to this request
            SsdpService ssdpService = mock(SsdpService.class);
            when(ssdpService.getRemoteIp()).thenReturn(inetAddress);
            when(ssdpService.getLocation()).thenReturn(location);
            when(ssdpService.getServiceType()).thenReturn("");
            serviceConsumer.accept(ssdpService);
        }
        return () -> {};
    }
}
