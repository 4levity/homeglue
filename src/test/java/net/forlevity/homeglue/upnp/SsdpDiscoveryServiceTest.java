/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.upnp;

import net.forlevity.homeglue.HomeglueTests;
import net.forlevity.homeglue.sim.SimulatedNetwork;
import net.forlevity.homeglue.sim.UpnpServiceInfo;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.LinkedBlockingQueue;

import static net.forlevity.homeglue.upnp.SsdpSearcher.ROOT_DEVICE_SERVICE_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class SsdpDiscoveryServiceTest extends HomeglueTests {

    @Test
    @SuppressWarnings("unchecked")
    public void testDiscoveryService() throws UnknownHostException, InterruptedException {
        // create sim device and network
        InetAddress remoteIp = InetAddress.getByName("10.1.2.3");
        Collection<UpnpServiceInfo> services = Collections.singleton(new UpnpServiceInfo(ROOT_DEVICE_SERVICE_TYPE, "uuid:1"));
        TestNetworkDevice device = new TestNetworkDevice(remoteIp, 9000, services);
        SimulatedNetwork network = new SimulatedNetwork(Collections.singleton(device));

        // create test service and register interest in our service
        SsdpDiscoveryServiceImpl service = new SsdpDiscoveryServiceImpl(network,0,0,0,0);
        LinkedBlockingQueue<SsdpServiceDefinition> queue = new LinkedBlockingQueue<>();
        service.registerSsdp(candidate -> candidate.getRemoteIp().equals(remoteIp), queue, 0);

        // run search and confirm we got our result
        service.runOnce();
        assertFalse(queue.isEmpty());
        assertEquals(remoteIp, queue.take().getRemoteIp());
    }
}
