/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.device.generic_upnp;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.log4j.Log4j2;
import net.forlevity.homeglue.device.AbstractDeviceManager;
import net.forlevity.homeglue.device.DeviceConnector;
import net.forlevity.homeglue.storage.DeviceStatusSink;
import net.forlevity.homeglue.upnp.SsdpDiscoveryService;
import net.forlevity.homeglue.upnp.SsdpServiceDefinition;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Implementation of DeviceManager that detects all other UPnP services that are NOT detected by other DeviceManagers.
 * Groups services as "devices" by host address. Can use that info to track whether a networked appliance is on or off.
 */
@Log4j2
@Singleton
public class GenericUpnpManager extends AbstractDeviceManager {

    private static final long STARTUP_DELAY_MILLIS = 5000;
    private final GenericUpnpConnectorFactory genericUpnpConnectorFactory;
    private final SsdpDiscoveryService ssdpDiscoveryService;
    private final LinkedBlockingQueue<SsdpServiceDefinition> discoveredServices = new LinkedBlockingQueue<>();
    private final Map<InetAddress, GenericUpnpConnector> devicesByAddress = new HashMap<>();

    @Inject
    protected GenericUpnpManager(GenericUpnpConnectorFactory genericUpnpConnectorFactory,
                                 SsdpDiscoveryService ssdpDiscoveryService,
                                 DeviceStatusSink deviceStatusSink) {
        super(deviceStatusSink);
        this.genericUpnpConnectorFactory = genericUpnpConnectorFactory;
        this.ssdpDiscoveryService = ssdpDiscoveryService;
    }

    @Override
    protected void run() throws Exception {
        // give other managers time to register first so we don't detect any of their devices
        Thread.sleep(STARTUP_DELAY_MILLIS);

        // low priority (high number) means pick up devices that are not registered to any other service
        ssdpDiscoveryService.registerSsdp(service -> true, discoveredServices, Integer.MAX_VALUE);

        // TODO: periodically update status of devices that have not connected recently

        while (true) {
            SsdpServiceDefinition service = discoveredServices.take();
            try {
                processServiceInfo(service);
            } catch (RuntimeException e) {
                log.error("unexpected exception processing UPnP service info (continuing)", e);
            }
        }
    }

    private void processServiceInfo(SsdpServiceDefinition service) {
        InetAddress address = service.getRemoteIp();
        GenericUpnpConnector genericUpnpDevice = devicesByAddress.get(address);
        if (genericUpnpDevice == null) {
            // new device (new IP address)
            genericUpnpDevice = genericUpnpConnectorFactory.create(address);
            genericUpnpDevice.add(service);
            if (genericUpnpDevice.connect()) {
                devicesByAddress.put(address, genericUpnpDevice);
                log.info ("other UPnP devices at: {}", Arrays.toString(devicesByAddress.keySet().toArray()));
                if (!genericUpnpDevice.getDeviceId().equals(DeviceConnector.DEVICE_ID_UNKNOWN)) {
                    register(genericUpnpDevice); // register if identifiable
                }
            }
        } else {
            // additional (or duplicate) service at same address
            genericUpnpDevice.add(service);
        }
    }
}
