/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.device.wemo;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import net.forlevity.homeglue.device.DeviceConnector;
import net.forlevity.homeglue.device.PowerMeterData;
import net.forlevity.homeglue.device.SoapHelper;
import net.forlevity.homeglue.util.Xml;
import org.w3c.dom.Document;

import java.io.IOException;
import java.util.Map;

/**
 * Implementation of DeviceConnector that connects to and reads meter data from a Belkin WeMo Insight plug meter.
 */
@Log4j2
@ToString(of = {"hostAddress"}, callSuper = true)
@EqualsAndHashCode(of = {"hostAddress", "port"}, callSuper = false)
public class WemoInsightConnector implements DeviceConnector {

    private static final String INSIGHT_SERVICE_URN = "urn:Belkin:service:insight:1";

    @Getter
    private String deviceId = DEVICE_ID_UNKNOWN;

    @Getter
    Map<String, String> deviceDetails = ImmutableMap.of();

    @Getter
    @Setter // port can change, device manager may update
    private int port;

    @Getter
    private boolean connected = false;

    private final SoapHelper soap;
    private final String hostAddress;

    @Inject
    WemoInsightConnector(SoapHelper soapHelper,
                         @Assisted String hostAddress,
                         @Assisted int port) {

        this.hostAddress = hostAddress;
        this.port = port;
        this.soap = soapHelper;
    }

    @Override
    public boolean connect() {
        String location = String.format("http://%s:%d/setup.xml", hostAddress, port);
        log.debug("trying to connect to wemo at {} ...", location);
        try {
            String result = soap.getHttpClient().get(location);
            connected = parseWemoSetup(result);
        } catch (IOException e) {
            log.warn("failed to get {} : {} {}", location, e.getClass().getSimpleName(), e.getMessage());
        }
        return connected;
    }

    /**
     * Extract meter metadata from setup.xml.
     * @param setupXml xml
     * @return true if successfully parsed setup.xml
     */
    private boolean parseWemoSetup(String setupXml) {
        Xml xml = soap.getXml();
        Document doc = xml.parse(setupXml);
        boolean success = false;
        String macAddress = xml.nodeText(doc, "/root/device/macAddress");
        if (macAddress == null) {
            log.warn("xml did not contain device macAddress");
        } else {
            this.deviceId = macAddress;
            this.deviceDetails = ImmutableMap.of(
                    "model", xml.nodeText(doc, "/root/device/modelDescription"),
                    "serialNumber", xml.nodeText(doc, "/root/device/serialNumber"),
                    "name", xml.nodeText(doc, "/root/device/friendlyName"),
                    "firmwareVersion", xml.nodeText(doc, "/root/device/firmwareVersion") );
            success = true;
        }
        return success;
    }

    Double read() {
        Double result = null;
        Document doc = execInsightSoapRequest("GetInsightParams");
        if (doc != null) {
            String insightParams = soap.getXml().nodeText(doc, "//InsightParams");
            if (insightParams != null) {
                String[] params = insightParams.split("\\|");
                double milliwatts = Double.valueOf(params[7]);
                log.debug("InsightParams={} / instantaneous power={} mw", insightParams, params[7], milliwatts);
                result = milliwatts / 1000.0;
            } else {
                log.warn("didn't get InsightParams from response");
            }
        } else {
            log.debug("failed to execute SOAP request for GetInsightParams");
        }
        return result;
    }

    private Document execInsightSoapRequest(String action) {
        String url = String.format("http://%s:%d/upnp/control/insight1", hostAddress, port);
        return soap.execSoapRequest(url, INSIGHT_SERVICE_URN, action);
    }
}
