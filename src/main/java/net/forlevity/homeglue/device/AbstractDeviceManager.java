/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.device;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.AbstractIdleService;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Synchronized;
import lombok.extern.log4j.Log4j2;
import net.forlevity.homeglue.entity.Device;
import net.forlevity.homeglue.persistence.PersistenceService;
import net.forlevity.homeglue.sink.DeviceStatus;
import net.forlevity.homeglue.sink.DeviceStatusChange;
import net.forlevity.homeglue.util.QueueProcessingThread;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Base class for DeviceManager implementations. Help maintain a list of devices.
 * Emits device status changes to consumer.
 */
@Log4j2
public abstract class AbstractDeviceManager extends AbstractIdleService implements DeviceManager {

    private final Map<String, DeviceConnector> devices = new HashMap<>();
    private final PersistenceService persistenceService;
    private final Consumer<DeviceStatusChange> deviceStatusChangeConsumer;
    private final QueueProcessingThread<DeviceStatus> deviceStatusProcessor;

    protected AbstractDeviceManager(PersistenceService persistenceService,
                                    Consumer<DeviceStatusChange> deviceStatusChangeConsumer) {
        this.persistenceService = persistenceService;
        this.deviceStatusChangeConsumer = deviceStatusChangeConsumer;
        this.deviceStatusProcessor = new QueueProcessingThread<>(DeviceStatus.class, this::processStatus);
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
     * status or other details changes.
     *
     * @param device the device
     */
    protected final void updateStatus(DeviceConnector device) {
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
     * @param reportedStatus status
     */
    private void processStatus(DeviceStatus reportedStatus) {
        if (!isRunning()) {
            return;
        }
        String deviceId = reportedStatus.getDeviceId();
        DeviceStatusChange statusChange = persistenceService.exec(session -> {
            Device device = session.bySimpleNaturalId(Device.class).load(deviceId);
            boolean changed = false;
            DeviceStatusChange result = null;
            if (device == null) {
                log.info("device first detection: {}", reportedStatus);
                device = Device.from(reportedStatus);
                changed = true;
            } else if (!device.sameAs(reportedStatus)) {
                device.setConnected(reportedStatus.isConnected());
                device.setDeviceDetails(reportedStatus.getDeviceDetails());
                changed = true;
            }
            if (changed) {
                session.saveOrUpdate(device);
                result = new DeviceStatusCopy(reportedStatus);
            }
            return result;
        });
        if (statusChange != null) {
            deviceStatusChangeConsumer.accept(statusChange);
        }
    }

    @Getter
    @EqualsAndHashCode
    private static class DeviceStatusCopy implements DeviceStatusChange {

        String deviceId;
        boolean connected;
        Map<String, String> deviceDetails;

        DeviceStatusCopy(DeviceStatus deviceStatus) {
            this.deviceId = deviceStatus.getDeviceId();
            this.connected = deviceStatus.isConnected();
            this.deviceDetails = ImmutableMap.copyOf(deviceStatus.getDeviceDetails());
        }
    }
}
