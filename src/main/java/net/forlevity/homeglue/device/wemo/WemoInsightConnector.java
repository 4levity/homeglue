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
import lombok.*;
import lombok.extern.log4j.Log4j2;
import net.forlevity.homeglue.device.*;
import net.forlevity.homeglue.entity.Device;
import net.forlevity.homeglue.util.Xml;
import org.w3c.dom.Document;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
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
    private static final Duration OFFLINE_DELAY = Duration.ofMinutes(1);
    private static final int MAX_CONSECUTIVE_READ_ERRORS = 3;

    @Getter
    private String detectionId = DEVICE_ID_UNKNOWN;

    @Getter
    Map<String, String> details = ImmutableMap.of();

    @Getter
    @Setter // port can change, device manager may update
    private int port;

    @Getter
    @Setter(AccessLevel.PACKAGE)
    private boolean connected = false;

    private final SoapHelper soap;
    private final DeviceConnectorInstances registry;
    private final Consumer<DeviceState> deviceStateConsumer;
    private final OfflineMarkerService offlineMarkerService;
    private final String hostAddress;
    private final PollerCommander poller;
    private int consecutiveReadErrors = 0;

    @Inject
    WemoInsightConnector(SoapHelper soapHelper,
                         DeviceConnectorInstances registry,
                         Consumer<DeviceState> deviceStateConsumer,
                         OfflineMarkerService offlineMarkerService, @Assisted String hostAddress, @Assisted int port, ScheduledExecutorService executor) {

        this.soap = soapHelper;
        this.registry = registry;
        this.deviceStateConsumer = deviceStateConsumer;
        this.offlineMarkerService = offlineMarkerService;
        this.hostAddress = hostAddress;
        this.port = port;
        poller = new PollerCommander(executor, getClass().getSimpleName() + "@" + hostAddress, this::poll,
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
                registry.register(this);
                poller.start();
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
            Command.Result result;
            Document doc = execWemoInsightSoapRequest(CONTROL_BASICEVENT, URN_BASICEVENT, ACTION_SETBINARYSTATE, params);
            if (doc != null) {
                // result has a regular insightparams field, so let's process that
                // since processing this command has probably caused us to miss a regular poll!
                String insightParams = soap.getXml().nodeText(doc, "//BinaryState");
                if (insightParams != null) {
                    DeviceState deviceState = processInsightParams(insightParams);
                    if (deviceState != null) {
                        deviceStateConsumer.accept(deviceState);
                        result = Command.Result.SUCCESS;
                    } else {
                        log.warn("couldn't parse BinaryState from SetBinaryState response");
                        result = Command.Result.CONNECTOR_ERROR;
                    }
                } else {
                    log.warn("missing BinaryState tag in SetBinaryState response");
                    result = Command.Result.DEVICE_ERROR;
                }
            } else {
                log.warn("didn't get response from SetBinaryState");
                result = Command.Result.COMMS_FAILED;
            }
            return result;
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
            this.detectionId = macAddress;
            this.details = ImmutableMap.of(
                    Device.DETAIL_MODEL, xml.nodeText(doc, "/root/device/modelDescription"),
                    Device.DETAIL_SERIAL_NUMBER, xml.nodeText(doc, "/root/device/serialNumber"),
                    Device.DETAIL_USER_SPECIFIED_NAME, xml.nodeText(doc, "/root/device/friendlyName"),
                    Device.DETAIL_FIRMWARE_VERSON, xml.nodeText(doc, "/root/device/firmwareVersion") );
            success = true;
        }
        return success;
    }

    boolean poll() {
        DeviceState deviceState = null;
        if (isConnected()) {
            try {
                deviceState = read();
            } catch(RuntimeException e) {
                log.error("unexpected exception during poll of {} (continuing)", this, e);
            }
            if (deviceState == null) {
                consecutiveReadErrors++;
                if (consecutiveReadErrors > MAX_CONSECUTIVE_READ_ERRORS) {
                    setConnected(false);
                    // mark offline right away, no need to wait for marker service to do it
                    offlineMarkerService.markOffline(detectionId);
                    // WemoInsightManagerService will turn us back on later, when SSDP service rediscovers the device
                }
            } else {
                consecutiveReadErrors = 0;
                deviceStateConsumer.accept(deviceState);
            }
        }
        return deviceState != null;
    }

    @VisibleForTesting
    DeviceState read() {
        DeviceState result = null;
        Document doc = execWemoInsightSoapRequest(CONTROL_INSIGHT, URN_INSIGHT, ACTION_INSIGHTPARAMS, "");
        if (doc != null) {
            String insightParams = soap.getXml().nodeText(doc, "//InsightParams");
            if (insightParams != null) {
                result = processInsightParams(insightParams);
            } else {
                log.warn("didn't get InsightParams from response");
            }
        } else {
            log.debug("failed to execute SOAP request for GetInsightParams");
        }
        return result;
    }

    private DeviceState processInsightParams(String insightParams) {
        String[] params = insightParams.split("\\|");
        if (params.length >= 8) {
            boolean switchClosed = !params[0].equals("0");
            double milliwatts = Double.valueOf(params[7]);
            log.debug("InsightParams={} / instantaneous power={} mw", insightParams, params[7]);
            return new DeviceState(this)
                    .setInstantaneousWatts(milliwatts / 1000.0)
                    .setRelayClosed(switchClosed);
        } // else
        return null;
    }

    private Document execWemoInsightSoapRequest(String control, String urn, String action, String content) {
        String url = String.format("http://%s:%d/upnp/control/%s", hostAddress, port, control);
        return soap.execSoapRequest(url, urn, action, content);
    }

    @Override
    public Duration getOfflineDelay() {
        return OFFLINE_DELAY;
    }
}
