/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.device;

import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.util.Map;

/**
 * State of a device at a point in time.
 */
@Getter
@Accessors(chain = true)
@ToString(of = {"detectionId", "deviceDetails", "timestamp"})
public class DeviceState implements DeviceInfo {

    private final Instant timestamp = Instant.now();

    @NonNull
    private final String detectionId;

    @Setter
    private Double instantaneousWatts = null;

    @Setter
    private Boolean relayClosed = null;

    @NonNull
    private final Map<String, String> deviceDetails;

    public DeviceState(String detectionId, Map<String, String> deviceDetails) {
        this.detectionId = detectionId;
        this.deviceDetails = ImmutableMap.copyOf(deviceDetails);
    }

    public DeviceState(String detectionId) {
        this(detectionId, ImmutableMap.of());
    }

    public DeviceState(DeviceInfo toCopy) {
        this(toCopy.getDetectionId(), toCopy.getDeviceDetails());
        setInstantaneousWatts(toCopy.getInstantaneousWatts());
        setRelayClosed(toCopy.getRelayClosed());
    }
}
