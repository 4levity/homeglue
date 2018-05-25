/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.sim;

import com.google.common.collect.ImmutableList;

import java.net.InetAddress;
import java.util.Collection;

/**
 * Lo-fi simulation of a Denon receiver with UPnP capabilities.
 */
public class SimulatedUpnpMediaPlayer extends SimulatedUpnpDevice {

    private static final String USN = "uuid:5f9ec1b3-ff59-19bb-8530-0005cda47215";

    private static final Collection<UpnpServiceInfo> services = ImmutableList.of(
            new UpnpServiceInfo(ROOT_DEVICE_SERVICE_TYPE, USN + "::" + ROOT_DEVICE_SERVICE_TYPE)
    );

    SimulatedUpnpMediaPlayer(InetAddress inetAddress, int upnpPort) {
        super(inetAddress, upnpPort, services);
    }

    @Override
    protected String getLocation() {
        return String.format("http://%s:%d/description.xml", inetAddress.getHostAddress(), getWebPort());
    }
}
