/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.device.wemo;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.extern.log4j.Log4j2;
import net.forlevity.homeglue.device.AbstractUpnpDeviceManager;
import net.forlevity.homeglue.device.PowerMeterConnector;
import net.forlevity.homeglue.sink.DeviceStatus;
import net.forlevity.homeglue.sink.PowerMeterData;
import net.forlevity.homeglue.upnp.SsdpDiscoveryService;
import net.forlevity.homeglue.upnp.SsdpServiceDefinition;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
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
            "http://(?<ipAddress>[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}):(?<port>[0-9]{4,5})/setup.xml");

    private final WemoInsightConnectorFactory connectorFactory;
    private final Consumer<PowerMeterData> telemetrySink;
    private final ConcurrentHashMap<String, WemoInsightConnector> insights = new ConcurrentHashMap<>();
    private final ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1);
    private final long pollPeriodMillis;

    private final Object meterReadLock = new Object();

    @Inject
    WemoInsightManager(SsdpDiscoveryService ssdpDiscoveryService,
                       WemoInsightConnectorFactory connectorFactory,
                       Consumer<DeviceStatus> deviceStatusSink,
                       Consumer<PowerMeterData> telemetrySink,
                       @Named("wemo.poll.period.millis") int pollPeriodMillis) {
        super(deviceStatusSink, ssdpDiscoveryService,
                service -> (SSDP_SERIALNUMBER.matcher(service.getSerialNumber()).matches()
                        && SSDP_LOCATION.matcher(service.getLocation()).matches()), 1);
        this.connectorFactory = connectorFactory;
        this.telemetrySink = telemetrySink;
        this.pollPeriodMillis = pollPeriodMillis;
    }

    @Override
    public void accept(SsdpServiceDefinition ssdpWemo) {
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
     * if the TCP webPort number has changed.
     * @param ipAddress IP address of discovered Insight
     * @param port TCP webPort of Insight
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
            log.info("WeMo Insight webPort at {} changed from {} to {}",
                    ipAddress, foundConnector.getPort(), port);
            foundConnector.setPort(port);
            executor.execute(() -> poll(foundConnector)); // immediate poll
        }
    }

    private void startTimer() {
        poll(); // first poll blocking on discovery thread, then start timer
        executor.scheduleAtFixedRate(this::poll, pollPeriodMillis, pollPeriodMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * Poll all Insights and send data to sink. Normally run by ScheduledExecutor.
     *
     * @return number of devices successfully polled
     */
    @VisibleForTesting
    int poll() {
        int[] polled = new int[1];
        insights.values().forEach(meter -> {
            if (poll(meter)) {
                polled[0]++;
            }
        });
        return polled[0];
    }

    private boolean poll(PowerMeterConnector meter) {
        PowerMeterData read = null;
        try {
            read = meter.read();
        } catch(RuntimeException e) {
            log.error("unexpected exception during poll of {} (continuing)", meter, e);
        }
        // TODO: handle failure to read meter
        try {
            telemetrySink.accept(read == null ? new PowerMeterData(meter.getDeviceId(), null) : read);
        } catch(RuntimeException e) {
            log.error("unexpected exception during storage of telemetry for {} (continuing)", meter, e);
        }
        return read != null;
    }

    @Override
    protected void shutDown() {
        executor.shutdown();
    }
}
