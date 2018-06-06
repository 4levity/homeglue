/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.device.generic_upnp;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import lombok.Getter;
import lombok.ToString;
import net.forlevity.homeglue.device.DeviceConnector;
import net.forlevity.homeglue.upnp.SsdpServiceDefinition;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A generic UPnP device is a collection of 1+ services detected at a particular host address.
 */
@ToString(of = {"detectionId"})
public class GenericUpnpConnector implements DeviceConnector {

    @Getter
    private final String detectionId;

    private final Set<SsdpServiceDefinition> ssdpServices = new HashSet<>();

    @Inject
    GenericUpnpConnector(@Assisted SsdpServiceDefinition firstService) {
        this.detectionId = firstService.getRemoteIp().getHostAddress();
        this.ssdpServices.add(firstService);
    }

    @Override
    public boolean start() {
        return true;
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public Map<String, String> getDeviceDetails() {
        // TODO: expose UPnP service information
        return ImmutableMap.of();
    }

    /**
     * Manager calls this to add a detected service at this address.
     *
     * @param service service info
     * @return true if this service was NOT already in the list
     */
    public boolean add(SsdpServiceDefinition service) {
        synchronized (ssdpServices) {
            return ssdpServices.add(service);
        }
    }
}