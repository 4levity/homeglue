/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.device;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import lombok.Synchronized;
import lombok.extern.log4j.Log4j2;
import net.forlevity.homeglue.storage.DeviceStatusSink;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Log4j2
public abstract class AbstractDeviceManager extends AbstractExecutionThreadService implements DeviceManager {

    private final DeviceStatusSink deviceStatusSink;
    private final Map<String, DeviceConnector> devices = new HashMap<>();

    protected AbstractDeviceManager(DeviceStatusSink deviceStatusSink) {
        this.deviceStatusSink = deviceStatusSink;
    }

    /**
     * Subclass should call this method once on the first connection to a device since the application started.
     *
     * @param device the device
     */
    protected final void register(DeviceConnector device) {
        updateDeviceMap(device);
        deviceStatusSink.accept(device);
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
}
