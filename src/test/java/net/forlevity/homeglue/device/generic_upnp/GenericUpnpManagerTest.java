/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.device.generic_upnp;

import com.google.common.collect.ImmutableList;
import lombok.extern.log4j.Log4j2;
import net.forlevity.homeglue.SimulatedNetworkTests;
import net.forlevity.homeglue.sim.SimulatedNetwork;
import net.forlevity.homeglue.storage.NoStorage;
import net.forlevity.homeglue.upnp.SsdpDiscoveryServiceImpl;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

@Log4j2
public class GenericUpnpManagerTest extends SimulatedNetworkTests {
    
    @Test
    public void genericUpnpManagerServiceTest() throws InterruptedException {
        SimulatedNetwork network = makeTestNetwork();
        GenericUpnpConnectorFactory factory = (hostAddress) -> new GenericUpnpConnector(network, hostAddress);
        SsdpDiscoveryServiceImpl ssdp = new SsdpDiscoveryServiceImpl(network, 0, 0, 0, 0);
        GenericUpnpManager manager = new GenericUpnpManager(ssdp, factory, new NoStorage());

        // disable SSDP service discovery on one of our devices
        device3.setServices(ImmutableList.of());

        ssdp.runOnce(); // run background search
        assertEquals(4, manager.getDiscoveredServicesQueue().size());
        manager.processDiscoveryQueue();
        // two unique devices should have been found
        assertEquals(2, manager.getDevices().size());

        // add another device and discover it
        device3.setServices(services3);
        ssdp.runOnce();
        assertEquals(8, manager.getDiscoveredServicesQueue().size());
        manager.processDiscoveryQueue();
        // one more unique device found
        assertEquals(3, manager.getDevices().size());
    }
}
