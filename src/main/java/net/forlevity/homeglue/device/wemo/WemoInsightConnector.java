package net.forlevity.homeglue.device.wemo;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import net.forlevity.homeglue.device.AbstractSoapDeviceConnector;
import net.forlevity.homeglue.device.PowerMeterConnector;
import net.forlevity.homeglue.device.PowerMeterData;
import net.forlevity.homeglue.http.SimpleHttpClient;
import org.w3c.dom.Document;

import java.io.IOException;

@Log4j2
@ToString(of = {"hostAddress"}, callSuper = true)
public class WemoInsightConnector extends AbstractSoapDeviceConnector implements PowerMeterConnector {

    private static final String INSIGHT_SERVICE_URN = "urn:Belkin:service:insight:1";
    private final String hostAddress;
    private final int port;

    @Getter
    private boolean connected = false;

    @Inject
    public WemoInsightConnector(SimpleHttpClient httpClient,
                                @Assisted String hostAddress,
                                @Assisted int port) {
        super(httpClient);
        this.hostAddress = hostAddress;
        this.port = port;
    }

    @Override
    public boolean connect() {
        String location = String.format("http://%s:%d/setup.xml", hostAddress, port);
        log.debug("trying to connect to wemo at {} ...", location);
        try {
            String result = getHttpClient().get(location);
            connected = parseWemoSetup(result);
        } catch (IOException e) {
            log.warn("failed to get {} : {} {}", location, e.getClass().getSimpleName(), e.getMessage());
        }
        return connected;
    }

    private boolean parseWemoSetup(String setupXml) {
        Document doc = parse(setupXml);
        boolean success = false;
        String macAddress = nodeText(doc, "/root/device/macAddress");
        if (macAddress == null) {
            log.warn("xml did not contain device macAddress");
        } else {
            this.setDeviceId(macAddress); // use MAC address as device ID
            this.setDeviceDetail("model", nodeText(doc, "/root/device/modelDescription"));
            this.setDeviceDetail("serialNumber", nodeText(doc, "/root/device/serialNumber"));
            this.setDeviceDetail("name", nodeText(doc, "/root/device/friendlyName"));
            this.setDeviceDetail("firmwareVersion", nodeText(doc, "/root/device/firmwareVersion"));
            success = true;
        }
        return success;
    }

    @Override
    public PowerMeterData read() {
        PowerMeterData result = null;
        Document doc = execInsightSoapRequest("GetInsightParams");
        String insightParams = nodeText(doc, "//InsightParams");
        if (insightParams != null) {
            String[] params = insightParams.split("\\|");
            double milliwatts = Double.valueOf(params[7]);
            log.debug("InsightParams={} / instantaneous power={} mw", insightParams, params[7], milliwatts);
            result = new PowerMeterData(milliwatts / 1000.0);
        } else {
            log.warn("didn't get InsightParams from response");
        }
        return result;
    }

    private Document execInsightSoapRequest(String action) {
        String url = String.format("http://%s:%d/upnp/control/insight1", hostAddress, port);
        return execSoapRequest(url, INSIGHT_SERVICE_URN, action);
    }
}
