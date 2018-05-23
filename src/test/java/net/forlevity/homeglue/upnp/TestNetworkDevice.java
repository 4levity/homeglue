/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.upnp;

import lombok.Getter;
import net.forlevity.homeglue.sim.AbstractSimulatedUpnpDevice;
import net.forlevity.homeglue.sim.UpnpServiceInfo;

import java.net.InetAddress;
import java.util.Collection;

@Getter
public class TestNetworkDevice extends AbstractSimulatedUpnpDevice {

    Collection<UpnpServiceInfo> services;

    String location;

    protected TestNetworkDevice(InetAddress inetAddress, int upnpPort, Collection<UpnpServiceInfo> services) {
        super(inetAddress, upnpPort);
        this.services = services;
        this.location = String.format("http://%s:%d/root.xml", inetAddress.getHostAddress(), upnpPort);
    }
}
