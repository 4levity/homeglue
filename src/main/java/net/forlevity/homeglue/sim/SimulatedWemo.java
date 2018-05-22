/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.sim;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.forlevity.homeglue.http.SimpleHttpClient;
import net.forlevity.homeglue.upnp.BackgroundProcessHandle;
import net.forlevity.homeglue.upnp.SsdpSearcher;
import net.forlevity.homeglue.upnp.SsdpServiceDefinition;
import net.forlevity.homeglue.util.ResourceHelper;
import net.forlevity.homeglue.util.Xml;
import org.apache.http.entity.ContentType;
import org.w3c.dom.Document;

import java.net.InetAddress;
import java.util.Map;
import java.util.function.Consumer;

@Log4j2
@Getter
public class SimulatedWemo implements SsdpSearcher, SimpleHttpClient {

    private static final String ERROR_RESPONSE = "error";

    private final Xml xml = new Xml();
    private final InetAddress inetAddress;
    private final int port;
    private final String location;
    private final String setupXml;
    private final String deviceSerialNumber;

    public SimulatedWemo(InetAddress inetAddress, int port, String setupXmlName) {
        this.inetAddress = inetAddress;
        this.port = port;
        this.location = String.format("http://%s:%d/setup.xml", inetAddress.getHostAddress(), port);
        this.setupXml = ResourceHelper.resourceAsString(setupXmlName);
        Document setupDocument = xml.parse(setupXml);
        this.deviceSerialNumber = xml.nodeText(setupDocument, "//serialNumber");
    }

    @Override
    public String get(String url) {
        if (url.endsWith("setup.xml")) {
            log.info("providing setup.xml for {}", deviceSerialNumber);
            return setupXml;
        }
        return ERROR_RESPONSE;
    }

    @Override
    public String post(String url, Map<String, String> headers, String payload, ContentType contentType) {
        if (url.endsWith("/upnp/control/insight1")
                && payload.equals(ResourceHelper.resourceAsString("sim/insightparams_request.xml"))
                && headers.get("SOAPAction").equals("\"urn:Belkin:service:insight:1#GetInsightParams\"")
                && contentType.equals(ContentType.TEXT_XML) ) {
            return ResourceHelper.resourceAsString("sim/insightparams_response.xml");
        }
        return ERROR_RESPONSE;
    }

    @Override
    public BackgroundProcessHandle startDiscovery(String serviceType,
                                                  Consumer<SsdpServiceDefinition> serviceConsumer) {
        // non-compliant WeMo only answers to specific service type request:
        if (serviceType.equals(ROOT_DEVICE_SERVICE_TYPE)) {
            // create mock service and send to consumer
            String serviceSerialNumber = String.format("uuid:Insight-1_0-%s::%s",
                    deviceSerialNumber, ROOT_DEVICE_SERVICE_TYPE);
            SsdpServiceDefinition service =
                    new SsdpServiceDefinition(serviceSerialNumber, ROOT_DEVICE_SERVICE_TYPE, location, inetAddress);
            serviceConsumer.accept(service);
        }
        return () -> {};
    }
}
