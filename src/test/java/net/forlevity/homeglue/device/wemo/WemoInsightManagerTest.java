/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.device.wemo;

import net.forlevity.homeglue.SimulatedNetworkTests;
import net.forlevity.homeglue.device.DeviceConnector;
import net.forlevity.homeglue.sim.SimulatedNetwork;
import net.forlevity.homeglue.sim.SimulatedWemo;
import net.forlevity.homeglue.storage.NoStorage;
import net.forlevity.homeglue.upnp.SsdpDiscoveryServiceImpl;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class WemoInsightManagerTest extends SimulatedNetworkTests {

    @Test
    public void testWemoInsightManager() throws InterruptedException {
        SimulatedWemo wemo1 = new SimulatedWemo(remoteIp4, 45678, "net/forlevity/homeglue/sim/insight1_setup.xml");
        SimulatedWemo wemo2 = new SimulatedWemo(remoteIp5, 45678, "net/forlevity/homeglue/sim/insight2_setup.xml");
        SimulatedNetwork network = makeTestNetwork(wemo1, wemo2);
        WemoInsightConnectorFactory factory = (hostAddress, port) -> new WemoInsightConnector(network, hostAddress, port);
        SsdpDiscoveryServiceImpl ssdp = new SsdpDiscoveryServiceImpl(network, 0, 0, 0, 0);
        NoStorage noStorage = new NoStorage();
        WemoInsightManager manager = new WemoInsightManager(ssdp, factory, noStorage, noStorage);

        // devices never connected
        assertEquals(0, manager.getDevices().size());

        ssdp.runOnce(); // run SSDP search
        assertEquals(2, manager.getDiscoveredServicesQueue().size());
        // two unique devices should have been found
        manager.processDiscoveryQueue();
        assertEquals(2, manager.getDevices().size());

        // devices have been connected to simulators
        manager.getDevices().forEach(device -> {
            assertEquals("Belkin Insight 1.0", device.getDeviceDetails().get("model"));
            assertNotNull(device.getDeviceDetails().get("name"));
            assertNotNull(device.getDeviceDetails().get("firmwareVersion"));
            assertNotNull(device.getDeviceDetails().get("serialNumber"));
        });
    }
}
