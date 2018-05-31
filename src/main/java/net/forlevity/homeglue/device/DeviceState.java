/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.device;

import com.google.common.collect.ImmutableMap;
import lombok.*;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.util.Map;

/**
 * State of a device at a point in time.
 */
@Getter
@Accessors(chain = true)
@ToString
@EqualsAndHashCode(of = { "deviceId", "connected", "instantaneousWatts", "deviceDetails" } )
public class DeviceState implements DeviceInfo {

    private final Instant timestamp = Instant.now();

    @NonNull
    private final String deviceId;

    private final boolean connected;

    @Setter
    private Double instantaneousWatts = null;

    @NonNull
    private final Map<String, String> deviceDetails;


    DeviceState(String deviceId, boolean connected, Map<String, String> deviceDetails) {
        this.deviceId = deviceId;
        this.connected = connected;
        this.deviceDetails = ImmutableMap.copyOf(deviceDetails);
    }

    public DeviceState(DeviceInfo toCopy) {
        this(toCopy.getDeviceId(), toCopy.isConnected(), toCopy.getDeviceDetails());
        setInstantaneousWatts(toCopy.getInstantaneousWatts());
    }
}
