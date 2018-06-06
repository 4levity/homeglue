/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.upnp;

import net.forlevity.homeglue.sim.SimulatedNetwork;
import net.forlevity.homeglue.sim.SimulatedUpnpDevice;
import net.forlevity.homeglue.sim.UpnpServiceInfo;
import net.forlevity.homeglue.testing.LinkedUniqueQueue;
import net.forlevity.homeglue.testing.SimulatedNetworkTests;
import net.forlevity.homeglue.util.ServiceDependencies;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.LinkedBlockingQueue;

import static net.forlevity.homeglue.upnp.SsdpSearcher.ROOT_DEVICE_SERVICE_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class SsdpDiscoveryServiceTest extends SimulatedNetworkTests {

    @Test
    @SuppressWarnings("unchecked")
    public void testDiscoveryService() throws UnknownHostException, InterruptedException {
        // create sim device and network
        InetAddress remoteIp = InetAddress.getByName("10.1.2.3");
        Collection<UpnpServiceInfo> services = Collections.singleton(new UpnpServiceInfo(ROOT_DEVICE_SERVICE_TYPE, "uuid:1"));
        SimulatedUpnpDevice device = new SimulatedUpnpDevice(remoteIp, 9000, services);
        SimulatedNetwork network = new SimulatedNetwork(Collections.singleton(device));

        // create test service and register interest in our service
        SsdpDiscoveryService service = new SsdpDiscoveryService(network, ServiceDependencies.NONE,0,0,0,0);
        LinkedBlockingQueue<SsdpServiceDefinition> queue = new LinkedBlockingQueue<>();
        service.registerSsdp(candidate -> candidate.getRemoteIp().equals(remoteIp), queue::offer, 0);

        // run search and confirm we got our result
        service.runOnce();
        assertFalse(queue.isEmpty());
        assertEquals(remoteIp, queue.take().getRemoteIp());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testDiscoveryPriority() throws InterruptedException {
        SimulatedNetwork network = makeTestNetwork();

        // create test service and register interest in our service
        SsdpDiscoveryService service = new SsdpDiscoveryService(network, ServiceDependencies.NONE,0,0,0,0);
        LinkedBlockingQueue<SsdpServiceDefinition> queueIp1 = new LinkedUniqueQueue<>();
        LinkedBlockingQueue<SsdpServiceDefinition> queueOtherRootDevices = new LinkedUniqueQueue<>();
        LinkedBlockingQueue<SsdpServiceDefinition> queueUsn3 = new LinkedUniqueQueue<>();
        // priority 1 queue gets anything at remoteIp1
        service.registerSsdp(candidate -> candidate.getRemoteIp().equals(remoteIp1), queueIp1::offer, 1);
        // priority 2 queue gets all root devices (except the one at remoteIp1)
        service.registerSsdp(candidate -> candidate.getServiceType().equals(ROOT_DEVICE_SERVICE_TYPE), queueOtherRootDevices::offer, 2);
        // priority 3 queue gets all services where serial starts with uuid:3 (except the one that's a root device)
        service.registerSsdp(candidate -> candidate.getSerialNumber().startsWith("uuid:3"), queueUsn3::offer, 3);

        // run search and confirm we got expected results
        service.runOnce();
        assertEquals(1, queueIp1.size());
        assertEquals("uuid:1", queueIp1.take().getSerialNumber());
        assertEquals(1, queueUsn3.size());
        assertEquals("urn:x", queueUsn3.take().getServiceType());
        assertEquals(2, queueOtherRootDevices.size());
    }
}
