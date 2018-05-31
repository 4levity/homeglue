/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.device;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.AbstractIdleService;
import lombok.Synchronized;
import lombok.extern.log4j.Log4j2;
import net.forlevity.homeglue.entity.Device;
import net.forlevity.homeglue.persistence.PersistenceService;
import net.forlevity.homeglue.util.QueueWorkerThread;

import java.util.*;
import java.util.function.Consumer;

/**
 * Base class for DeviceManager implementations. Help maintain a list of devices.
 * Emits device status changes to consumer.
 */
@Log4j2
public abstract class AbstractDeviceManager extends AbstractIdleService implements DeviceManager {

    private final Map<String, DeviceConnector> devices = new HashMap<>();
    private final PersistenceService persistenceService;
    private final Consumer<DeviceEvent> deviceEventConsumer;
    private final QueueWorkerThread<DeviceConnector> deviceStatusProcessor;

    protected AbstractDeviceManager(PersistenceService persistenceService,
                                    Consumer<DeviceEvent> deviceEventConsumer) {
        this.persistenceService = persistenceService;
        this.deviceEventConsumer = deviceEventConsumer;
        this.deviceStatusProcessor = new QueueWorkerThread<>(DeviceConnector.class, this::processStatus);
    }

    @Override
    protected void startUp() throws Exception {
        persistenceService.awaitRunning();
        deviceStatusProcessor.start();
    }

    @Override
    protected void shutDown() throws Exception {
        deviceStatusProcessor.interrupt();
    }

    /**
     * Subclass should call this method once on the first connection to a
     * device since the application started, and any time the connection
     * status or device details might have changed. Devices may re-submit
     * unchanged status periodically but should limit the rate.
     *
     * @param device the device
     */
    protected final void reportStatus(DeviceConnector device) {
        updateDeviceMap(device);
        deviceStatusProcessor.accept(device);
    }

    @Override
    @Synchronized("devices")
    public Collection<DeviceConnector> getDevices() {
        return ImmutableList.copyOf(devices.values());
    }

    @Synchronized("devices")
    private void updateDeviceMap(DeviceConnector device) {
        DeviceConnector existingDevice = devices.get(device.getDeviceId());
        if (existingDevice != null && existingDevice != device) {
            log.warn("there is more than one DeviceConnector instance with deviceId = {}", device.getDeviceId());
        }
        devices.put(device.getDeviceId(), device);
    }

    /**
     * Runs on queue processing thread.
     *
     * @param newDeviceStatus device status
     */
    private void processStatus(DeviceStatus newDeviceStatus) {
        String deviceId = newDeviceStatus.getDeviceId();
        List<DeviceEvent> newEvents = persistenceService.exec(session -> {
            Device device = session.bySimpleNaturalId(Device.class).load(deviceId);
            List<DeviceEvent> events = new ArrayList<>();
            if (device == null) {
                log.info("device first detection: {}", device);
                device = Device.from(newDeviceStatus);
                events.add(new DeviceEvent(deviceId, DeviceEvent.NEW_DEVICE, device.getDeviceDetails()));
            } else {
                if (device.isConnected() != newDeviceStatus.isConnected()) {
                    device.setConnected(newDeviceStatus.isConnected());
                    String event = device.isConnected() ? DeviceEvent.CONNECTED : DeviceEvent.CONNECTION_LOST;
                    events.add(new DeviceEvent(deviceId, event));
                }
                if (!device.getDeviceDetails().equals(newDeviceStatus.getDeviceDetails())) {
                    device.setDeviceDetails(newDeviceStatus.getDeviceDetails());
                    events.add(new DeviceEvent(deviceId, DeviceEvent.DETAILS_CHANGED, device.getDeviceDetails()));
                }
            }
            if (events.size() > 0) {
                session.saveOrUpdate(device);
            }
            return events;
        });
        newEvents.forEach(deviceEventConsumer::accept);
    }
}
