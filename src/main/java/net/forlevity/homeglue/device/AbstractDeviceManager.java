/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.device;

import com.google.common.collect.ImmutableList;
import lombok.Synchronized;
import lombok.extern.log4j.Log4j2;
import net.forlevity.homeglue.util.RunnableExecutionThreadService;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Base class for DeviceManager implementations. Help maintain a list of devices.
 * Emits device status changes to consumer.
 */
@Log4j2
public abstract class AbstractDeviceManager extends RunnableExecutionThreadService implements DeviceManager {

    private final Consumer<DeviceStatus> deviceStatusConsumer;
    private final Map<String, DeviceConnector> devices = new HashMap<>();

    protected AbstractDeviceManager(Consumer<DeviceStatus> deviceStatusConsumer) {
        this.deviceStatusConsumer = deviceStatusConsumer;
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
        deviceStatusConsumer.accept(device);
    }

    @Override
    @Synchronized("devices")
    public Collection<DeviceConnector> getDevices() {
        return ImmutableList.copyOf(devices.values());
    }

    @Override
    public DeviceConnector getDevice(String deviceId) {
        return devices.get(deviceId);
    }

    @Synchronized("devices")
    private void updateDeviceMap(DeviceConnector device) {
        DeviceConnector existingDevice = devices.get(device.getDeviceId());
        if (existingDevice != null && existingDevice != device) {
            log.warn("there is more than one DeviceConnector instance with deviceId = {}", device.getDeviceId());
        }
        devices.put(device.getDeviceId(), device);
    }
}
