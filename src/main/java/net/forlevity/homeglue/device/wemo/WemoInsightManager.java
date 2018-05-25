/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.device.wemo;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.log4j.Log4j2;
import net.forlevity.homeglue.device.AbstractUpnpDeviceManager;
import net.forlevity.homeglue.device.PowerMeterConnector;
import net.forlevity.homeglue.storage.DeviceStatusSink;
import net.forlevity.homeglue.storage.PowerMeterData;
import net.forlevity.homeglue.storage.TelemetrySink;
import net.forlevity.homeglue.upnp.SsdpDiscoveryService;
import net.forlevity.homeglue.upnp.SsdpServiceDefinition;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * DeviceManager implementation that detects all Belkin WeMo Insight meters on the LAN and polls them periodically.
 */
@Log4j2
@Singleton
public class WemoInsightManager extends AbstractUpnpDeviceManager {

    private static final Pattern SSDP_SERIALNUMBER = Pattern.compile("uuid:Insight-1.*");
    private static final Pattern SSDP_LOCATION = Pattern.compile(
            "http://(?<ipAddress>[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}):(?<port>[0-9]{5})/setup.xml");
    private static final long SCAN_PERIOD_MILLIS = 2500L;

    private final WemoInsightConnectorFactory connectorFactory;
    private final TelemetrySink telemetrySink;
    private final ConcurrentHashMap<String, WemoInsightConnector> insights = new ConcurrentHashMap<>();
    private final ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1);

    @Inject
    WemoInsightManager(SsdpDiscoveryService ssdpDiscoveryService,
                       WemoInsightConnectorFactory connectorFactory,
                       DeviceStatusSink deviceStatusSink, TelemetrySink telemetrySink) {
        super(deviceStatusSink, ssdpDiscoveryService,
                service -> (SSDP_SERIALNUMBER.matcher(service.getSerialNumber()).matches()
                        && SSDP_LOCATION.matcher(service.getLocation()).matches()), 1);
        this.connectorFactory = connectorFactory;
        this.telemetrySink = telemetrySink;
    }

    @Override
    protected void processDiscoveredService(SsdpServiceDefinition ssdpWemo) {
        Matcher location = SSDP_LOCATION.matcher(ssdpWemo.getLocation());
        if (location.matches()) {
            String ipAddress = location.group("ipAddress");
            int port = Integer.valueOf(location.group("port"));
            handleWemoDiscovery(ipAddress, port);
        } else {
            log.warn("thought we found Insight meter but has unexpected location: {}", ssdpWemo.getLocation());
        }
    }

    /**
     * Upon hearing of a new Insight meter, if it is new then try to connect to it and register it for future polling.
     * If it's the first meter we have detected, start polling. If it's a meter we already know about, check to see
     * if the TCP upnpPort number has changed.
     * @param ipAddress IP address of discovered Insight
     * @param port TCP upnpPort of Insight
     */
    private void handleWemoDiscovery(String ipAddress, int port) {
        WemoInsightConnector foundConnector = insights.get(ipAddress);
        if (foundConnector == null) {
            WemoInsightConnector newConnector = connectorFactory.create(ipAddress, port);
            if (newConnector.connect()) {
                log.info("connected to Insight meter at {}:{}", ipAddress, port);
                boolean firstDevice = insights.isEmpty();
                insights.put(ipAddress, newConnector);
                register(newConnector);
                if (firstDevice) {
                    // as soon as the first device is found, start polling
                    startTimer();
                }
            } else {
                log.warn("detected but failed to connect to Insight meter at {}:{}", ipAddress, port);
            }
        } else if (foundConnector.getPort() != port) {
            log.info("WeMo Insight upnpPort at {} changed from {} to {}",
                    ipAddress, foundConnector.getPort(), port);
            foundConnector.setPort(port);
        }
    }

    private void startTimer() {
        executor.scheduleAtFixedRate(() -> poll(), 0, SCAN_PERIOD_MILLIS, TimeUnit.MILLISECONDS);
    }

    private void poll() {
        insights.values().forEach(this::poll);
    }

    private void poll(PowerMeterConnector meter) {
        try {
            PowerMeterData read = meter.read();
            // TODO: handle failure to read meter
            telemetrySink.accept(meter.getDeviceId(), read);
        } catch(RuntimeException e) {
            log.error("unexpected exception during poll of {} (continuing)", meter, e);
        }
    }

    @Override
    protected void shutDown() {
        executor.shutdown();
    }
}
