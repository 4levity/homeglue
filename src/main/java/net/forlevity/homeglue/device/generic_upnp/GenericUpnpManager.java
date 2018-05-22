/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.device.generic_upnp;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.resourcepool.ssdp.model.SsdpService;
import lombok.extern.log4j.Log4j2;
import net.forlevity.homeglue.device.AbstractDeviceManager;
import net.forlevity.homeglue.device.DeviceConnector;
import net.forlevity.homeglue.storage.DeviceStatusSink;
import net.forlevity.homeglue.upnp.SsdpDiscoveryService;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

@Log4j2
@Singleton
public class GenericUpnpManager extends AbstractDeviceManager {

    private static final long STARTUP_DELAY_MILLIS = 5000;
    private final GenericUpnpConnectorFactory genericUpnpConnectorFactory;
    private final SsdpDiscoveryService ssdpDiscoveryService;
    private final LinkedBlockingQueue<SsdpService> discoveredServices = new LinkedBlockingQueue<>();
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
        // give other managers time to register so we don't see their devices
        Thread.sleep(STARTUP_DELAY_MILLIS);

        // pick up devices that are not registered to any other service
        ssdpDiscoveryService.registerSsdp(service -> true, discoveredServices, Integer.MAX_VALUE);

        // TODO: periodically update status of devices that have not connected recently

        while (true) {
            SsdpService service = discoveredServices.take();
            processServiceInfo(service);
        }
    }

    private void processServiceInfo(SsdpService service) {
        InetAddress address = service.getRemoteIp();
        GenericUpnpConnector genericUpnpDevice = devicesByAddress.get(address);
        if (genericUpnpDevice == null) {
            // new device (new IP address)
            genericUpnpDevice = genericUpnpConnectorFactory.create(address);
            genericUpnpDevice.add(service);
            if (genericUpnpDevice.connect()) {
                devicesByAddress.put(address, genericUpnpDevice);
                log.info ("there are UPNP devices at: {}", Arrays.toString(devicesByAddress.keySet().toArray()));
                if (!genericUpnpDevice.getDeviceId().equals(DeviceConnector.DEVICE_ID_UNKNOWN)) {
                    register(genericUpnpDevice); // register if identifiable
                }
            }
        } else if (!genericUpnpDevice.has(service)) {
            // additional service at same address
            genericUpnpDevice.add(service);
        }
    }
}
