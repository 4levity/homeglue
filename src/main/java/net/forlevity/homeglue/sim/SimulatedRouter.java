/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.sim;

import com.google.common.collect.ImmutableList;
import lombok.Getter;

import java.net.InetAddress;
import java.util.List;

/**
 * Lo-fi simulation of OpenWRT router with UPnP service enabled.
 */
@Getter
public class SimulatedRouter extends AbstractSimulatedUpnpDevice {

    private static String USN1 = "uuid:2447cd11-081b-4225-ad70-0d9de3aed106";
    private static String USN2 = "uuid:2447cd11-081b-4225-ad70-0d9de3aed107";
    private static String USN3 = "uuid:2447cd11-081b-4225-ad70-0d9de3aed108";
    private static String L3FWD = "urn:schemas-upnp-org:service:Layer3Forwarding:1";
    private static String WANPPP = "urn:schemas-upnp-org:service:WANPPPConnection:1";
    private static String WANIP = "urn:schemas-upnp-org:service:WANIPConnection:1";
    private static String WANCOMMON = "urn:schemas-upnp-org:service:WANCommonInterfaceConfig:1";
    private static String WANDEVICE = "urn:schemas-upnp-org:device:WANDevice:1";
    private static String WANCONNECTION = "urn:schemas-upnp-org:device:WANConnectionDevice:1";
    private static String GATEWAY = "urn:schemas-upnp-org:device:InternetGatewayDevice:1";

    List<UpnpServiceMock> services = ImmutableList.of(
            new UpnpServiceMock(ROOT_DEVICE_SERVICE_TYPE, USN1 + "::" + ROOT_DEVICE_SERVICE_TYPE),
            new UpnpServiceMock(USN1, USN1),
            new UpnpServiceMock(USN2, USN2),
            new UpnpServiceMock(USN3, USN3),
            new UpnpServiceMock(L3FWD, USN1 + "::" + L3FWD),
            new UpnpServiceMock(WANPPP, USN3 + "::" + WANPPP),
            new UpnpServiceMock(WANIP, USN3 + "::" + WANIP),
            new UpnpServiceMock(WANCOMMON, USN2 + "::" + WANCOMMON),
            new UpnpServiceMock(WANDEVICE, USN2 + "::" + WANDEVICE),
            new UpnpServiceMock(WANCONNECTION, USN2 + "::" + WANCONNECTION),
            new UpnpServiceMock(GATEWAY, USN1 + "::" + GATEWAY)
    );

    protected SimulatedRouter(InetAddress inetAddress, int upnpPort) {
        super(inetAddress, upnpPort);
    }

    @Override
    public String getLocation() {
        return String.format("http://%s:%d/rootDesc.xml", getInetAddress().getHostAddress(), getUpnpPort());
    }
}
