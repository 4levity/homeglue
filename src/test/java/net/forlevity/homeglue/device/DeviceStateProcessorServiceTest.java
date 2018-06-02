/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.device;

import com.google.common.collect.ImmutableMap;
import net.forlevity.homeglue.entity.ApplianceDetector;
import net.forlevity.homeglue.entity.Device;
import net.forlevity.homeglue.entity.Relay;
import net.forlevity.homeglue.testing.FakePersistence;
import net.forlevity.homeglue.testing.HomeglueTests;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;

public class DeviceStateProcessorServiceTest extends HomeglueTests {

    @Test
    public void testNewDevice() {
        List<DeviceEvent> events = new ArrayList<>();
        FakePersistence persistence = new FakePersistence();
        DeviceStateProcessorService processor = new DeviceStateProcessorService(null, persistence, new ApplianceStateDecider(), events::add);

        // event generated
        DeviceState state = new DeviceState("new", true);
        processor.handle(state);
        assertEquals(1, events.size());
        assertEquals(DeviceEvent.NEW_DEVICE, events.get(0).getEvent());
        assertEquals("new", events.get(0).getDeviceId());

        // device saved
        ArgumentCaptor<Device> deviceArgumentCaptor = ArgumentCaptor.forClass(Device.class);
        verify(persistence.getSession()).saveOrUpdate(deviceArgumentCaptor.capture());
        assertEquals("new", deviceArgumentCaptor.getValue().getDeviceId());
    }

    @Test
    public void testNewDeviceWithRelayAndMeter() {
        List<DeviceEvent> events = new ArrayList<>();
        FakePersistence persistence = new FakePersistence();
        DeviceStateProcessorService processor = new DeviceStateProcessorService(null, persistence, new ApplianceStateDecider(), events::add);

        // event generated
        DeviceState state = new DeviceState("new", true, ImmutableMap.of("key", "value"))
                .setRelayClosed(true).setInstantaneousWatts(25.0);
        processor.handle(state);
        assertEquals(1, events.size());
        assertEquals(DeviceEvent.NEW_DEVICE, events.get(0).getEvent());
        assertEquals("new", events.get(0).getDeviceId());
        assertEquals(state.getDeviceDetails(), events.get(0).getData());
        events.clear();

        // device saved
        ArgumentCaptor<Device> deviceArgumentCaptor = ArgumentCaptor.forClass(Device.class);
        verify(persistence.getSession()).saveOrUpdate(deviceArgumentCaptor.capture());
        Device device = deviceArgumentCaptor.getValue();
        assertEquals("new", device.getDeviceId());
        assertTrue(device.getRelay().isClosed());
        assertTrue(device.getApplianceDetector().isOn());
    }

    @Test
    public void changeConnectionState() {
        List<DeviceEvent> events = new ArrayList<>();
        Device existingDevice = Device.from(new DeviceState("devid", true));
        FakePersistence persistence = new FakePersistence().setResolver(id -> existingDevice);
        DeviceStateProcessorService processor = new DeviceStateProcessorService(null, persistence, new ApplianceStateDecider(), events::add);

        processor.handle(new DeviceState("devid", false));
        ArgumentCaptor<Device> deviceArgumentCaptor = ArgumentCaptor.forClass(Device.class);
        verify(persistence.getSession()).saveOrUpdate(deviceArgumentCaptor.capture());
        assertEquals("devid", deviceArgumentCaptor.getValue().getDeviceId());
        assertFalse(deviceArgumentCaptor.getValue().isConnected());

        processor.handle(new DeviceState("devid", true));
        processor.handle(new DeviceState("devid", true));
        processor.handle(new DeviceState("devid", true));
        assertEquals(2, events.size());
        assertEquals(DeviceEvent.CONNECTION_LOST, events.get(0).getEvent());
        assertEquals("devid", events.get(0).getDeviceId());
        assertEquals(DeviceEvent.CONNECTED, events.get(1).getEvent());
        assertEquals("devid", events.get(1).getDeviceId());
    }

    @Test
    public void changeDeviceDetails() {
        List<DeviceEvent> events = new ArrayList<>();
        Map<String, String> originalDetails = ImmutableMap.of("k1", "v1", "k2", "v2");
        Device existingDevice = Device.from(new DeviceState("devid", true, originalDetails));
        FakePersistence persistence = new FakePersistence().setResolver(id -> existingDevice);
        DeviceStateProcessorService processor = new DeviceStateProcessorService(null, persistence, new ApplianceStateDecider(), events::add);

        Map<String, String> newDeviceDetails = ImmutableMap.of("k1","changed_v1","k2","v2");
        processor.handle(new DeviceState("devid", true, newDeviceDetails));

        // saved?
        ArgumentCaptor<Device> deviceArgumentCaptor = ArgumentCaptor.forClass(Device.class);
        verify(persistence.getSession()).saveOrUpdate(deviceArgumentCaptor.capture());
        assertEquals("devid", deviceArgumentCaptor.getValue().getDeviceId());
        assertEquals(newDeviceDetails, deviceArgumentCaptor.getValue().getDetails());

        DeviceState newState = new DeviceState("devid", true, originalDetails);
        processor.handle(newState);
        processor.handle(newState);
        processor.handle(newState); // multiple invocations no extra events
        assertEquals(2, events.size());
        assertEquals(DeviceEvent.DETAILS_CHANGED, events.get(0).getEvent());
        assertEquals(newDeviceDetails, events.get(0).getData());
        assertEquals("devid", events.get(0).getDeviceId());
        assertEquals(DeviceEvent.DETAILS_CHANGED, events.get(1).getEvent());
        assertEquals(originalDetails, events.get(1).getData());
        assertEquals("devid", events.get(1).getDeviceId());
    }

    @Test
    public void relayDetected() {
        List<DeviceEvent> events = new ArrayList<>();
        DeviceState initialState = new DeviceState("devid", true);
        Device device = Device.from(initialState);
        FakePersistence persistence = new FakePersistence().setResolver(id -> device);
        DeviceStateProcessorService processor = new DeviceStateProcessorService(null, persistence, new ApplianceStateDecider(), events::add);

        assertNull(device.getRelay());
        processor.handle(new DeviceState(initialState).setRelayClosed(true));
        processor.handle(new DeviceState(initialState).setRelayClosed(true));

        // saved?
        ArgumentCaptor<Device> deviceArgumentCaptor = ArgumentCaptor.forClass(Device.class);
        verify(persistence.getSession()).saveOrUpdate(deviceArgumentCaptor.capture());
        assertEquals("devid", deviceArgumentCaptor.getValue().getDeviceId());
        Relay relay = device.getRelay();
        assertEquals(device, relay.getDevice());
        assertTrue(relay.isClosed());

        assertEquals(0, events.size()); // no event for initial detection

        processor.handle(new DeviceState(initialState).setRelayClosed(false));
        processor.handle(new DeviceState(initialState).setRelayClosed(false));
        processor.handle(new DeviceState(initialState).setRelayClosed(true));
        processor.handle(new DeviceState(initialState).setRelayClosed(true));
        assertEquals(2, events.size());
        assertEquals(DeviceEvent.RELAY_OPENED, events.get(0).getEvent());
        assertEquals("devid", events.get(0).getDeviceId());
        assertEquals(DeviceEvent.RELAY_CLOSED, events.get(1).getEvent());
        assertEquals("devid", events.get(1).getDeviceId());
    }

    @Test
    public void powerMeterDetected() {
        List<DeviceEvent> events = new ArrayList<>();
        DeviceState initialState = new DeviceState("did", true);
        Device device = Device.from(initialState);
        FakePersistence persistence = new FakePersistence().setResolver(id -> device);
        DeviceStateProcessorService processor = new DeviceStateProcessorService(null, persistence, new ApplianceStateDecider(), events::add);

        assertNull(device.getRelay());
        processor.handle(new DeviceState(initialState).setInstantaneousWatts(0.5));

        // saved?
        ArgumentCaptor<Device> deviceArgumentCaptor = ArgumentCaptor.forClass(Device.class);
        verify(persistence.getSession()).saveOrUpdate(deviceArgumentCaptor.capture());
        assertEquals("did", deviceArgumentCaptor.getValue().getDeviceId());
        ApplianceDetector applianceDetector = device.getApplianceDetector();
        assertEquals(device, applianceDetector.getDevice());
        assertFalse(applianceDetector.isOn());

        assertEquals(0, events.size()); // no event for initial detection

        processor.handle(new DeviceState(initialState).setInstantaneousWatts(1000.0));
        processor.handle(new DeviceState(initialState).setInstantaneousWatts(1000.0));
        processor.handle(new DeviceState(initialState).setInstantaneousWatts(0.5));
        processor.handle(new DeviceState(initialState).setInstantaneousWatts(0.5));
        assertEquals(2, events.size());
        assertEquals(DeviceEvent.APPLIANCE_ON, events.get(0).getEvent());
        assertEquals("did", events.get(0).getDeviceId());
        assertEquals(DeviceEvent.APPLIANCE_OFF, events.get(1).getEvent());
        assertEquals("did", events.get(1).getDeviceId());

        events.clear();
        // reconfigure appliance detector
        applianceDetector.setMinWatts(0.1f);
        processor.handle(new DeviceState(initialState).setInstantaneousWatts(0.5));
        processor.handle(new DeviceState(initialState).setInstantaneousWatts(0.5));
        processor.handle(new DeviceState(initialState).setInstantaneousWatts(0.0));
        assertEquals(2, events.size());
        assertEquals(DeviceEvent.APPLIANCE_ON, events.get(0).getEvent());
        assertEquals("did", events.get(0).getDeviceId());
        assertEquals(DeviceEvent.APPLIANCE_OFF, events.get(1).getEvent());
        assertEquals("did", events.get(1).getDeviceId());
    }
}
