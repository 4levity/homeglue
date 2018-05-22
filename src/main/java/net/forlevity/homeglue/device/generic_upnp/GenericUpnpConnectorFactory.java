package net.forlevity.homeglue.device.generic_upnp;

import java.net.InetAddress;

public interface GenericUpnpConnectorFactory {

    GenericUpnpConnector create(InetAddress hostAddress);
}
