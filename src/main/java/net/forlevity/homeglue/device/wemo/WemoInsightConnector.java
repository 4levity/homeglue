/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.device.wemo;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import net.forlevity.homeglue.device.*;
import net.forlevity.homeglue.util.Xml;
import org.w3c.dom.Document;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * Implementation of DeviceConnector that connects to and reads meter data from a Belkin WeMo Insight plug meter.
 */
@Log4j2
@ToString(of = {"hostAddress"}, callSuper = true)
@EqualsAndHashCode(of = {"hostAddress", "port"}, callSuper = false)
public class WemoInsightConnector implements DeviceConnector {

    private static final String URN_INSIGHT = "urn:Belkin:service:insight:1";
    private static final String CONTROL_INSIGHT = "insight1";
    private static final String ACTION_INSIGHTPARAMS = "GetInsightParams";

    private static final String URN_BASICEVENT = "urn:Belkin:service:basicevent:1";
    private static final String CONTROL_BASICEVENT = "basicevent1";
    private static final String ACTION_SETBINARYSTATE = "SetBinaryState";

    private static final int POLLING_PERIOD_MILLIS = 2500;
    private static final int MIN_IDLE_MILLIS = 2000;

    @Getter
    private String deviceId = DEVICE_ID_UNKNOWN;

    @Getter
    Map<String, String> deviceDetails = ImmutableMap.of();

    @Getter
    @Setter // port can change, device manager may update
    private int port;

    @Getter
    private boolean connected = false;

    @VisibleForTesting
    @Setter
    private boolean pollingEnabled = true;

    private final SoapHelper soap;
    private final DeviceCommandDispatcher dispatcher;
    private final Consumer<DeviceState> deviceStateConsumer;
    private final String hostAddress;
    private final PollerCommander poller;

    @Inject
    WemoInsightConnector(SoapHelper soapHelper,
                         DeviceCommandDispatcher dispatcher,
                         Consumer<DeviceState> deviceStateConsumer,
                         @Assisted String hostAddress,
                         @Assisted int port) {

        this.hostAddress = hostAddress;
        this.port = port;
        this.soap = soapHelper;
        this.dispatcher = dispatcher;
        this.deviceStateConsumer = deviceStateConsumer;
        poller = new PollerCommander(getClass().getSimpleName() + "@" + hostAddress, this::poll,
                POLLING_PERIOD_MILLIS, MIN_IDLE_MILLIS);
    }

    @Override
    public boolean start() {
        String location = String.format("http://%s:%d/setup.xml", hostAddress, port);
        log.debug("trying to connect to wemo at {} ...", location);
        try {
            String result = soap.getHttpClient().get(location);
            connected = parseWemoSetup(result);
            if (connected) {
                dispatcher.register(this);
                if (pollingEnabled) {
                    poller.start();
                }
            }
        } catch (IOException e) {
            log.warn("failed to get {} : {} {}", location, e.getClass().getSimpleName(), e.getMessage());
        }
        return connected;
    }

    @Override
    public void terminate() {
        if (poller.isStarted()) {
            poller.stop();
        }
    }

    @Override
    public Future<Command.Result> dispatch(Command command) {
        if (!isConnected()) {
            return CompletableFuture.completedFuture(Command.Result.COMMS_FAILED);
        }
        switch (command.getAction()) {
            case OPEN_RELAY:
                return changeRelay(true);
            case CLOSE_RELAY:
                return changeRelay(false);
            default:
                log.warn("unsupported command {}", command);
                return CompletableFuture.completedFuture(Command.Result.NOT_SUPPORTED);
        }
    }

    private Future<Command.Result> changeRelay(boolean closed) {
        String params = String.format("<BinaryState>%c</BinaryState>", closed ? '0' : '1');
        return poller.runCommand(() -> {
            Document doc = execWemoInsightSoapRequest(CONTROL_BASICEVENT, URN_BASICEVENT, ACTION_SETBINARYSTATE, params);
            // TODO: check result
            return Command.Result.SUCCESS;
        });
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

    boolean poll() {
        DeviceState deviceState = null;
        try {
            deviceState = read();
        } catch(RuntimeException e) {
            log.error("unexpected exception during poll of {} (continuing)", this, e);
        }
        // TODO: handle failure to read meter
        try {
            if (deviceState != null) {
                deviceStateConsumer.accept(deviceState);
            }
        } catch(RuntimeException e) {
            log.error("unexpected exception during storage of telemetry for {} (continuing)", this, e);
        }
        return deviceState != null;
    }

    @VisibleForTesting
    DeviceState read() {
        Double watts = null;
        Boolean switchClosed = null;
        Document doc = execWemoInsightSoapRequest(CONTROL_INSIGHT, URN_INSIGHT, ACTION_INSIGHTPARAMS, "");
        if (doc != null) {
            String insightParams = soap.getXml().nodeText(doc, "//InsightParams");
            if (insightParams != null) {
                String[] params = insightParams.split("\\|");
                switchClosed = !params[0].equals("0");
                double milliwatts = Double.valueOf(params[7]);
                log.debug("InsightParams={} / instantaneous power={} mw", insightParams, params[7], milliwatts);
                watts = milliwatts / 1000.0;
            } else {
                log.warn("didn't get InsightParams from response");
            }
        } else {
            log.debug("failed to execute SOAP request for GetInsightParams");
        }
        return watts == null ? null : new DeviceState(this)
                .setInstantaneousWatts(watts)
                .setRelayClosed(switchClosed);
    }

    private Document execWemoInsightSoapRequest(String control, String urn, String action, String content) {
        String url = String.format("http://%s:%d/upnp/control/%s", hostAddress, port, control);
        return soap.execSoapRequest(url, urn, action, content);
    }
}
