/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.device.wemo;

import lombok.extern.log4j.Log4j2;
import net.forlevity.homeglue.device.DeviceConnectorInstances;
import net.forlevity.homeglue.device.LastDeviceStateCache;
import net.forlevity.homeglue.device.OfflineMarkerService;
import net.forlevity.homeglue.device.SoapHelper;
import net.forlevity.homeglue.persistence.PersistenceService;
import net.forlevity.homeglue.sim.SimulatedNetwork;
import net.forlevity.homeglue.sim.SimulatedWemo;
import net.forlevity.homeglue.testing.SimulatedNetworkTests;
import net.forlevity.homeglue.upnp.SsdpDiscoveryService;
import org.junit.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Log4j2
public class WemoInsightManagerServiceTest extends SimulatedNetworkTests {

    SimulatedNetwork network;
    WemoInsightManagerService manager;
    SsdpDiscoveryService ssdp;
    LastDeviceStateCache telemetryCache;

    @Test
    public void testWemoInsightManager() throws InterruptedException {
        SimulatedWemo wemo1 = new SimulatedWemo(remoteIp4, 45678, "net/forlevity/homeglue/sim/insight1_setup.xml");
        SimulatedWemo wemo2 = new SimulatedWemo(remoteIp5, 45678, "net/forlevity/homeglue/sim/insight2_setup.xml");
        makeWemoManager(wemo1, wemo2);

        // devices never connected
        assertEquals(0, manager.getDevices().size());

        ssdp.runOnce(); // run SSDP search
        assertEquals(2, manager.getQueue().size());
        // two unique devices should have been found
        manager.processQueue();
        assertEquals(2, manager.getDevices().size());

        // devices have been connected to simulators
        manager.getDevices().values().forEach(device -> {
            assertEquals("Belkin Insight 1.0", device.getDeviceDetails().get("model"));
            assertNotNull(device.getDeviceDetails().get("name"));
            assertNotNull(device.getDeviceDetails().get("firmwareVersion"));
            assertNotNull(device.getDeviceDetails().get("serialNumber"));
        });
    }

    private void makeWemoManager(SimulatedWemo... wemos) {
        network = makeTestNetwork(wemos);
        SoapHelper soapHelper = new SoapHelper(network);
        telemetryCache = new LastDeviceStateCache();
        WemoInsightConnectorFactory factory = (hostAddress, port) -> new WemoInsightConnector(soapHelper,
                mock(DeviceConnectorInstances.class), telemetryCache, mock(OfflineMarkerService.class), hostAddress, port, mock(ScheduledExecutorService.class)
        );
        ssdp = new SsdpDiscoveryService(network);
        PersistenceService persistence = mock(PersistenceService.class);
        when(persistence.exec(any())).thenReturn(new ArrayList<>());
        manager = new WemoInsightManagerService(null, ssdp, factory, 2500);
    }

    @Test
    public void changeWemoInsightPortNumber() throws InterruptedException {
        SimulatedWemo simulator = new SimulatedWemo(remoteIp4, 2000, "net/forlevity/homeglue/sim/insight1_setup.xml");
        String macAddress = simulator.getMacAddress();
        assertEquals(12, macAddress.length());
        makeWemoManager(simulator);
        ssdp.runOnce();
        assertEquals(1, manager.getQueue().size());
        manager.processQueue();
        assertEquals(1, manager.getDevices().size());
        WemoInsightConnector device = manager.getDevices().values().iterator().next();
        assertEquals(macAddress, device.getDetectionId());
        assertEquals(2000, device.getPort());
        assertTrue(device.poll()); // first poll
        Instant lastTelemetryTime = telemetryCache.lastDeviceState.get(macAddress).getTimestamp();

        // timestamp changes because successful poll
        assertTrue(device.poll());
        Instant newTelemetryTime = telemetryCache.lastDeviceState.get(macAddress).getTimestamp();
        assertNotEquals(lastTelemetryTime, newTelemetryTime);
        lastTelemetryTime = newTelemetryTime;

        // now this time the poll should have failed
        simulator.setWebPort(3000);
        assertFalse(device.poll());

        // now we rescan and device manager should pick up the new port
        ssdp.runOnce();
        manager.processQueue();
        assertEquals(1, manager.getDevices().size());
        device = manager.getDevices().values().iterator().next();
        assertEquals(3000, device.getPort());

        // next poll works and telemetry timestamp changes
        assertTrue(device.poll());
        newTelemetryTime = telemetryCache.lastDeviceState.get(macAddress).getTimestamp();
        assertNotEquals(lastTelemetryTime, newTelemetryTime);
    }
}
