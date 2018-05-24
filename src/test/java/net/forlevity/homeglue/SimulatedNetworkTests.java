/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.forlevity.homeglue.sim.SimulatedNetwork;
import net.forlevity.homeglue.sim.SimulatedNetworkDevice;
import net.forlevity.homeglue.sim.SimulatedUpnpDevice;
import net.forlevity.homeglue.sim.UpnpServiceInfo;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Collections;

import static net.forlevity.homeglue.upnp.SsdpSearcher.ROOT_DEVICE_SERVICE_TYPE;

public abstract class SimulatedNetworkTests extends HomeglueTests {

    protected final InetAddress remoteIp1;
    protected final InetAddress remoteIp2;
    protected final InetAddress remoteIp3;
    protected final InetAddress remoteIp4;
    protected final InetAddress remoteIp5;

    protected final Collection<UpnpServiceInfo> services1 = Collections.singleton(new UpnpServiceInfo(ROOT_DEVICE_SERVICE_TYPE, "uuid:1"));
    protected final Collection<UpnpServiceInfo> services2 = Collections.singleton(new UpnpServiceInfo(ROOT_DEVICE_SERVICE_TYPE, "uuid:2"));
    protected final Collection<UpnpServiceInfo> services3 = ImmutableList.of(
            new UpnpServiceInfo(ROOT_DEVICE_SERVICE_TYPE, "uuid:3"),
            new UpnpServiceInfo("urn:x", "uuid:3::urn:x"));

    protected SimulatedUpnpDevice device1;
    protected SimulatedUpnpDevice device2;
    protected SimulatedUpnpDevice device3;

    public SimulatedNetworkTests() {
        try {
            remoteIp1 = InetAddress.getByName("10.0.0.1");
            remoteIp2 = InetAddress.getByName("10.0.0.2");
            remoteIp3 = InetAddress.getByName("10.0.0.3");
            remoteIp4 = InetAddress.getByName("10.0.0.4");
            remoteIp5 = InetAddress.getByName("10.0.0.5");
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    protected SimulatedNetwork makeTestNetwork(SimulatedNetworkDevice... moreDevices) {
        // create sim devices and network
        device1 = new SimulatedUpnpDevice(remoteIp1, 9000, services1);
        device2 = new SimulatedUpnpDevice(remoteIp2, 9000, services2);
        device3 = new SimulatedUpnpDevice(remoteIp3, 9000, services3);
        Collection<SimulatedNetworkDevice> devices = Lists.newArrayList(moreDevices);
        devices.add(device1);
        devices.add(device2);
        devices.add(device3);
        return new SimulatedNetwork(devices);
    }
}
