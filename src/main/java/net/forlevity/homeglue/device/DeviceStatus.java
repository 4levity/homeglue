/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.device;

import com.google.common.collect.ImmutableMap;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

/**
 * Status of a device at a point in time.
 */
@Getter
public class DeviceStatus implements DeviceInfo {

    private final Instant timestamp = Instant.now();

    private final String deviceId;
    private final boolean connected;
    private final Map<String, String> deviceDetails;

    DeviceStatus(String deviceId, boolean connected, Map<String, String> deviceDetails) {
        this.deviceId = deviceId;
        this.connected = connected;
        this.deviceDetails = ImmutableMap.copyOf(deviceDetails);
    }

    public DeviceStatus(DeviceInfo toCopy) {
        this(toCopy.getDeviceId(), toCopy.isConnected(), toCopy.getDeviceDetails());
    }
}
