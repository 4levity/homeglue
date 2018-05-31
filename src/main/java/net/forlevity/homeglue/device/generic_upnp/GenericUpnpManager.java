/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.device.generic_upnp;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.log4j.Log4j2;
import net.forlevity.homeglue.device.AbstractUpnpDeviceManager;
import net.forlevity.homeglue.device.DeviceConnector;
import net.forlevity.homeglue.device.DeviceStatus;
import net.forlevity.homeglue.upnp.SsdpDiscoveryService;
import net.forlevity.homeglue.upnp.SsdpServiceDefinition;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Implementation of DeviceManager that detects all other UPnP services that are NOT detected by other DeviceManagers.
 * Groups services as "devices" by host address. Can use that info to track whether a networked appliance is on or off.
 */
@Log4j2
@Singleton
public class GenericUpnpManager extends AbstractUpnpDeviceManager {

    private final GenericUpnpConnectorFactory genericUpnpConnectorFactory;

    private final Map<InetAddress, GenericUpnpConnector> devicesByAddress = new HashMap<>();

    @Inject
    GenericUpnpManager(SsdpDiscoveryService ssdpDiscoveryService,
                       GenericUpnpConnectorFactory genericUpnpConnectorFactory,
                       Consumer<DeviceStatus> deviceStatusSink) {
        super(deviceStatusSink, ssdpDiscoveryService, service -> true, Integer.MAX_VALUE);
        this.genericUpnpConnectorFactory = genericUpnpConnectorFactory;
        // TODO: periodically update status of devices that have not been re-discovered recently
    }

    @Override
    public void notifyServiceDiscovered(SsdpServiceDefinition service) {
        InetAddress address = service.getRemoteIp();
        GenericUpnpConnector genericUpnpDevice = devicesByAddress.get(address);
        if (genericUpnpDevice == null) {
            // new device (new IP address)
            genericUpnpDevice = genericUpnpConnectorFactory.create(service);
            if (genericUpnpDevice.connect()) {
                devicesByAddress.put(address, genericUpnpDevice);
                log.info ("other UPnP devices at: {}", Arrays.toString(devicesByAddress.keySet().toArray()));
                if (!genericUpnpDevice.getDeviceId().equals(DeviceConnector.DEVICE_ID_UNKNOWN)) {
                    reportStatus(genericUpnpDevice); // register if identifiable
                }
            }
        } else {
            // additional (or duplicate) service at same address
            genericUpnpDevice.add(service);
        }
    }
}
