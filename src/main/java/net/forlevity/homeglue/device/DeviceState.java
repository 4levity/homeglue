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
@ToString(of = {"detectionId", "details", "timestamp"})
public class DeviceState implements DeviceInfo {

    private final Instant timestamp = Instant.now();

    @NonNull
    private final String detectionId;

    @Setter
    private Double instantaneousWatts = null;

    @Setter
    private Boolean relayClosed = null;

    private final Map<String, String> details;

    public DeviceState(String detectionId, Map<String, String> details) {
        this.detectionId = detectionId;
        this.details = details == null ? null : ImmutableMap.copyOf(details);
    }

    public DeviceState(String detectionId) {
        this(detectionId, null);
    }

    public DeviceState(DeviceInfo toCopy) {
        this(toCopy.getDetectionId(), null);
        setInstantaneousWatts(toCopy.getInstantaneousWatts());
        setRelayClosed(toCopy.getRelayClosed());
    }
}
