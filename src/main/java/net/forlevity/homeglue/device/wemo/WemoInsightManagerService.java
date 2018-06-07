/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.device.wemo;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.forlevity.homeglue.device.DeviceConnector;
import net.forlevity.homeglue.upnp.SsdpDiscoveryService;
import net.forlevity.homeglue.upnp.SsdpServiceDefinition;
import net.forlevity.homeglue.util.QueueWorkerService;
import net.forlevity.homeglue.util.ServiceDependencies;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * DeviceManager implementation that detects all Belkin WeMo Insight meters on the LAN and polls them periodically.
 */
@Log4j2
@Singleton
public class WemoInsightManagerService extends QueueWorkerService<SsdpServiceDefinition> {

    private static final Pattern SSDP_SERIALNUMBER = Pattern.compile("uuid:Insight-1.*");
    private static final Pattern SSDP_LOCATION = Pattern.compile(
            "http://(?<ipAddress>[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}):(?<port>[0-9]{4,5})/setup.xml");

    @Getter
    private final Map<String, WemoInsightConnector> devices = new ConcurrentHashMap<>();

    private final WemoInsightConnectorFactory connectorFactory;
    private final long pollPeriodMillis;

    @Inject
    WemoInsightManagerService(ServiceDependencies dependencies,
                              SsdpDiscoveryService ssdpDiscoveryService,
                              WemoInsightConnectorFactory connectorFactory,
                              @Named("wemo.poll.period.millis") int pollPeriodMillis) {
        super(SsdpServiceDefinition.class, dependencies);
        ssdpDiscoveryService.registerSsdp(
                service -> (SSDP_SERIALNUMBER.matcher(service.getSerialNumber()).matches()
                        && SSDP_LOCATION.matcher(service.getLocation()).matches()), this,1);
        this.connectorFactory = connectorFactory;
        this.pollPeriodMillis = pollPeriodMillis;
    }

    /**
     * Handle incoming WeMo Insight devices discovered from UPnP/SSDP service.
     *
     * @param ssdpWemo discovered device
     */
    @Override
    public void handle(SsdpServiceDefinition ssdpWemo) {
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
        WemoInsightConnector match = devices.get(ipAddress);
        if (match == null) {
            WemoInsightConnector newConnector = connectorFactory.create(ipAddress, port);
            if (newConnector.start()) {
                log.info("connected to Insight meter at {}:{}", ipAddress, port);
                devices.put(ipAddress, newConnector);
            } else {
                log.warn("detected but failed to connect to Insight meter at {}:{}", ipAddress, port);
            }
        } else {
            // re-discovered existing wemo
            if (match.getPort() != port) {
                log.info("WeMo Insight webPort at {} changed from {} to {}", ipAddress, match.getPort(), port);
                match.setPort(port);
            }
            if (!match.isConnected()) {
                match.setConnected(true);
            }
        }
        // if no new device or port change, ignore
    }

    @Override
    protected void shutDown() throws Exception {
        devices.values().forEach(DeviceConnector::terminate);
    }
}
