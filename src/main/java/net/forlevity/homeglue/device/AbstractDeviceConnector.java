/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.device;

import com.google.common.collect.ImmutableMap;
import lombok.*;

import java.util.Map;

/**
 * Base class for DeviceConnector implementations. Subclass MUST call setDeviceId() and SHOULD call setDeviceDetail().
 */
@ToString(of = {"deviceId"})
public abstract class AbstractDeviceConnector implements DeviceConnector {

    @Getter
    @Setter(AccessLevel.PROTECTED)
    private String deviceId = DEVICE_ID_UNKNOWN;

    private Map<String, String> deviceDetails = ImmutableMap.of();
    private final Object deviceDetailsLock = new Object();

    @Override
    @Synchronized("deviceDetailsLock")
    public Map<String, String> getDeviceDetails() {
        return deviceDetails;
    }

    /**
     * Subclass should call this whenever metadata about a device is retrieved. May call repeatedly for the same data.
     * @param deviceDetails map
     */
    @Synchronized("deviceDetailsLock")
    protected void setDeviceDetails(Map<String, String> deviceDetails) {
        this.deviceDetails = ImmutableMap.copyOf(deviceDetails);
    }
}
