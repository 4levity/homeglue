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
import net.forlevity.homeglue.sink.DeviceStatus;
import net.forlevity.homeglue.sink.DeviceStatusChange;

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
    private final Map<String, DeviceStatusCopy> statusCache = new HashMap<>();
    private final Consumer<DeviceStatusChange> deviceStatusChangeConsumer;

    protected AbstractDeviceManager(Consumer<DeviceStatusChange> deviceStatusChangeConsumer) {
        this.deviceStatusChangeConsumer = deviceStatusChangeConsumer;
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
        processStatus(device);
    }

    @Synchronized("statusCache")
    private void processStatus(DeviceStatus deviceStatus) {
        String deviceId = deviceStatus.getDeviceId();
        DeviceStatusCopy newStatus = new DeviceStatusCopy(deviceStatus);
        DeviceStatus cached = statusCache.get(deviceId);
        boolean changed = false;
        if (cached == null) {
            // TODO: check persistent storage
            changed = true;
        } else if (!cached.equals(newStatus)) {
            changed = true;
        }
        if (changed) {
            statusCache.put(deviceId, newStatus);
            deviceStatusChangeConsumer.accept(newStatus);
            // TODO: update persistent storage
        }

    }

    @Override
    @Synchronized("devices")
    public Collection<DeviceConnector> getDevices() {
        return ImmutableList.copyOf(devices.values());
    }

    @Synchronized("devices")
    private void updateDeviceMap(DeviceConnector device) {
        if (devices.containsKey(device.getDeviceId())) {
            log.warn("more than one device registered with deviceId = {}", device.getDeviceId());
        }
        devices.put(device.getDeviceId(), device);
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
