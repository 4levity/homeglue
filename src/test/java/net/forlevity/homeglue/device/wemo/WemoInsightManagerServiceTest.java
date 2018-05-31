/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.device.wemo;

import com.google.common.collect.ImmutableSet;
import lombok.extern.log4j.Log4j2;
import net.forlevity.homeglue.device.LastTelemetryCache;
import net.forlevity.homeglue.device.PowerMeterData;
import net.forlevity.homeglue.device.SoapHelper;
import net.forlevity.homeglue.sim.SimulatedNetwork;
import net.forlevity.homeglue.sim.SimulatedWemo;
import net.forlevity.homeglue.sink.TelemetryLogger;
import net.forlevity.homeglue.testing.SimulatedNetworkTests;
import net.forlevity.homeglue.upnp.SsdpDiscoveryService;
import net.forlevity.homeglue.util.FanoutExchange;
import org.junit.Test;

import java.time.Instant;
import java.util.function.Consumer;

import static org.junit.Assert.*;

@Log4j2
public class WemoInsightManagerServiceTest extends SimulatedNetworkTests {

    SimulatedNetwork network;
    WemoInsightManagerService manager;
    SsdpDiscoveryService ssdp;
    LastTelemetryCache telemetryCache;

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
        WemoInsightConnectorFactory factory = (hostAddress, port) -> new WemoInsightConnector(soapHelper, hostAddress, port);
        ssdp = new SsdpDiscoveryService(network, 0, 0, 0, 0);
        telemetryCache = new LastTelemetryCache();
        Consumer<PowerMeterData> exchange = new FanoutExchange<>(ImmutableSet.of(telemetryCache, new TelemetryLogger()));
        manager = new WemoInsightManagerService(ssdp, factory, status -> log.info("{}", status), exchange, 2500);
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
        manager.processQueue();
        assertEquals(1, manager.getDevices().size());
        device = manager.getDevices().values().iterator().next();
        assertEquals(3000, device.getPort());

        // poll was triggered by port change, so within a couple ms it has also polled the device successfully again
        Thread.sleep(50);
        newTelemetryTime = telemetryCache.lastPowerMeterData.get(macAddress).getTimestamp();
        assertNotEquals(lastTelemetryTime, newTelemetryTime);
    }
}
