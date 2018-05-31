/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.device.generic_upnp;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import lombok.ToString;
import net.forlevity.homeglue.device.AbstractDeviceConnector;
import net.forlevity.homeglue.upnp.SsdpServiceDefinition;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

/**
 * A generic UPnP device is a collection of 1+ services detected at a particular host address.
 */
@ToString(of = {"hostAddress"}, callSuper = true)
public class GenericUpnpConnector extends AbstractDeviceConnector {

    private final InetAddress hostAddress;
    private final Set<SsdpServiceDefinition> ssdpServices = new HashSet<>();

    @Inject
    GenericUpnpConnector(@Assisted SsdpServiceDefinition firstService) {
        this.hostAddress = firstService.getRemoteIp();
        this.ssdpServices.add(firstService);
        setDeviceId(firstService.getRemoteIp().getHostAddress());
    }

    @Override
    public boolean connect() {
        return true;
    }

    @Override
    public boolean isConnected() {
        return true;
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