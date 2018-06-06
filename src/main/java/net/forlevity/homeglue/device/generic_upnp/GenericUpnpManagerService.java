/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.device.generic_upnp;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.forlevity.homeglue.device.DeviceConnector;
import net.forlevity.homeglue.device.DeviceState;
import net.forlevity.homeglue.upnp.SsdpDiscoveryService;
import net.forlevity.homeglue.upnp.SsdpServiceDefinition;
import net.forlevity.homeglue.util.QueueWorkerService;
import net.forlevity.homeglue.util.ServiceDependencies;

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
public class GenericUpnpManagerService extends QueueWorkerService<SsdpServiceDefinition> {

    private final GenericUpnpConnectorFactory genericUpnpConnectorFactory;

    @Getter
    private final Map<String, GenericUpnpConnector> devices = new HashMap<>();
    private final Consumer<DeviceState> deviceStateConsumer;

    @Inject
    GenericUpnpManagerService(ServiceDependencies dependencies,
                              SsdpDiscoveryService ssdpDiscoveryService,
                              GenericUpnpConnectorFactory genericUpnpConnectorFactory,
                              Consumer<DeviceState> deviceStateConsumer) {
        super(SsdpServiceDefinition.class, dependencies);
        ssdpDiscoveryService.registerSsdp(service -> true, this, Integer.MAX_VALUE);
        this.genericUpnpConnectorFactory = genericUpnpConnectorFactory;
        this.deviceStateConsumer = deviceStateConsumer;
        // TODO: periodically update status of devices that have not been re-discovered recently
    }

    @Override
    public void handle(SsdpServiceDefinition service) {
        InetAddress address = service.getRemoteIp();
        String hostAddress = address.getHostAddress();
        GenericUpnpConnector genericUpnpDevice = devices.get(hostAddress);
        if (genericUpnpDevice == null) {
            // new device (new IP address)
            genericUpnpDevice = genericUpnpConnectorFactory.create(service);
            if (genericUpnpDevice.start()) {
                devices.put(hostAddress, genericUpnpDevice);
                log.info ("other UPnP devices at: {}", Arrays.toString(devices.keySet().toArray()));
                if (!genericUpnpDevice.getDetectionId().equals(DeviceConnector.DEVICE_ID_UNKNOWN)) {
                    deviceStateConsumer.accept(new DeviceState(genericUpnpDevice)); // register if identifiable
                }
            }
        } else {
            // additional (or duplicate) service at same address
            genericUpnpDevice.add(service);
        }
    }
}
