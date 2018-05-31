/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.device.wemo;

import com.google.common.collect.ImmutableSet;
import net.forlevity.homeglue.device.LastTelemetryCache;
import net.forlevity.homeglue.device.PowerMeterData;
import net.forlevity.homeglue.persistence.PersistenceService;
import net.forlevity.homeglue.sim.SimulatedNetwork;
import net.forlevity.homeglue.sim.SimulatedWemo;
import net.forlevity.homeglue.sink.DeviceEventLogger;
import net.forlevity.homeglue.sink.TelemetryLogger;
import net.forlevity.homeglue.testing.SimulatedNetworkTests;
import net.forlevity.homeglue.upnp.SsdpDiscoveryServiceImpl;
import net.forlevity.homeglue.util.FanoutExchange;
import org.junit.Test;

import java.time.Instant;
import java.util.function.Consumer;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class WemoInsightManagerTest extends SimulatedNetworkTests {

    SimulatedNetwork network;
    WemoInsightManager manager;
    SsdpDiscoveryServiceImpl ssdp;
    LastTelemetryCache telemetryCache;

    @Test
    public void testWemoInsightManager() throws InterruptedException {
        SimulatedWemo wemo1 = new SimulatedWemo(remoteIp4, 45678, "net/forlevity/homeglue/sim/insight1_setup.xml");
        SimulatedWemo wemo2 = new SimulatedWemo(remoteIp5, 45678, "net/forlevity/homeglue/sim/insight2_setup.xml");
        makeWemoManager(wemo1, wemo2);

        // devices never connected
        assertEquals(0, manager.getDevices().size());

        ssdp.runOnce(); // run SSDP search
        assertEquals(2, manager.getDiscoveryProcessor().getQueue().size());
        // two unique devices should have been found
        manager.getDiscoveryProcessor().processQueue();
        assertEquals(2, manager.getDevices().size());

        // devices have been connected to simulators
        manager.getDevices().forEach(device -> {
            assertEquals("Belkin Insight 1.0", device.getDeviceDetails().get("model"));
            assertNotNull(device.getDeviceDetails().get("name"));
            assertNotNull(device.getDeviceDetails().get("firmwareVersion"));
            assertNotNull(device.getDeviceDetails().get("serialNumber"));
        });
    }

    private void makeWemoManager(SimulatedWemo... wemos) {
        network = makeTestNetwork(wemos);
        WemoInsightConnectorFactory factory = (hostAddress, port) -> new WemoInsightConnector(network, hostAddress, port);
        ssdp = new SsdpDiscoveryServiceImpl(network, 0, 0, 0, 0);
        telemetryCache = new LastTelemetryCache();
        Consumer<PowerMeterData> exchange = new FanoutExchange<>(ImmutableSet.of(telemetryCache, new TelemetryLogger()));
        manager = new WemoInsightManager(mock(PersistenceService.class), ssdp, factory, new DeviceEventLogger(), exchange, 2500);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void changeWemoInsightPortNumber() throws InterruptedException {
        SimulatedWemo simulator = new SimulatedWemo(remoteIp4, 2000, "net/forlevity/homeglue/sim/insight1_setup.xml");
        String macAddress = simulator.getMacAddress();
        assertEquals(12, macAddress.length());
        makeWemoManager(simulator);
        ssdp.runOnce();
        assertEquals(1, manager.getDiscoveryProcessor().getQueue().size());
        manager.getDiscoveryProcessor().processQueue();
        assertEquals(1, manager.getDevices().size());
        WemoInsightConnector device = (WemoInsightConnector) manager.getDevices().iterator().next();
        assertEquals(macAddress, device.getDeviceId());
        assertEquals(2000, device.getPort());
        Instant lastTelemetryTime = telemetryCache.lastPowerMeterData.get(macAddress).getTimestamp();

        // timestamp changes because successful poll
        assertEquals(1, manager.poll());
        Instant newTelemetryTime = telemetryCache.lastPowerMeterData.get(macAddress).getTimestamp();
        assertNotEquals(lastTelemetryTime, newTelemetryTime);
        lastTelemetryTime = newTelemetryTime;

        // now this time the poll should have failed
        simulator.setWebPort(3000);
        assertEquals(0, manager.poll());

        // now we rescan and device manager should pick up the new port
        ssdp.runOnce();
        manager.getDiscoveryProcessor().processQueue();
        assertEquals(1, manager.getDevices().size());
        device = (WemoInsightConnector) manager.getDevices().iterator().next();
        assertEquals(3000, device.getPort());

        // poll was triggered by port change, so within a couple ms it has also polled the device successfully again
        Thread.sleep(50);
        newTelemetryTime = telemetryCache.lastPowerMeterData.get(macAddress).getTimestamp();
        assertNotEquals(lastTelemetryTime, newTelemetryTime);
    }
}
