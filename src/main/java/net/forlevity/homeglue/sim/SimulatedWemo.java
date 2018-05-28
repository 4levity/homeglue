/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.sim;

import com.google.common.annotations.VisibleForTesting;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.forlevity.homeglue.upnp.SafeCloseable;
import net.forlevity.homeglue.upnp.SsdpServiceDefinition;
import net.forlevity.homeglue.util.ResourceHelper;
import org.apache.http.entity.ContentType;
import org.w3c.dom.Document;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Lo-fi simulation of a Belkin WeMo Insight plug meter device.
 */
@Log4j2
@Getter
public class SimulatedWemo extends SimulatedUpnpDevice {

    private final String setupXml;
    private final String deviceSerialNumber;
    private final String macAddress;

    @VisibleForTesting
    public SimulatedWemo(InetAddress inetAddress, int port, String location) {
        super(inetAddress, port);
        this.setupXml = ResourceHelper.resourceAsString(location);
        Document setupDocument = xml.parse(setupXml);
        this.deviceSerialNumber = xml.nodeText(setupDocument, "//serialNumber");
        this.macAddress = xml.nodeText(setupDocument, "//macAddress");
        setServices(Collections.singleton(new UpnpServiceInfo(ROOT_DEVICE_SERVICE_TYPE,
                String.format("uuid:Insight-1_0-%s::%s", deviceSerialNumber, ROOT_DEVICE_SERVICE_TYPE))) );
    }

    @Override
    protected String getLocation() {
        return String.format("http://%s:%d/setup.xml", getInetAddress().getHostAddress(), getWebPort());
    }

    @Override
    public String get(String url) throws IOException {
        if (url.equals(getLocation())) {
            log.info("providing setup.xml for {}", deviceSerialNumber);
            return setupXml;
        }
        return super.get(url);
    }

    @Override
    public String post(String url, Map<String, String> headers, String payload, ContentType contentType)
            throws IOException {
        String expected = String.format("http://%s:%d/upnp/control/insight1",
                getInetAddress().getHostAddress(), getWebPort());
        if (url.equals(expected)
                && payload.equals(ResourceHelper.resourceAsString("net/forlevity/homeglue/sim/insightparams_request.xml"))
                && headers.get("SOAPAction").equals("\"urn:Belkin:service:insight:1#GetInsightParams\"")
                && contentType.equals(ContentType.TEXT_XML) ) {
            return ResourceHelper.resourceAsString("net/forlevity/homeglue/sim/insightparams_response.xml");
        }
        return super.post(url, headers, payload, contentType);
    }

    @Override
    public SafeCloseable startDiscovery(String serviceType,
                                        Consumer<SsdpServiceDefinition> serviceConsumer) {
        // non-compliant WeMo only answers to specific service type request:
        if (serviceType != null && serviceType.equals(ROOT_DEVICE_SERVICE_TYPE)) {
            super.startDiscovery(serviceType, serviceConsumer);
        }
        return () -> {};
    }
}
