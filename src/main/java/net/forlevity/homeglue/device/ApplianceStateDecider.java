/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.device;

import com.google.common.base.Preconditions;
import com.google.inject.Singleton;
import lombok.extern.log4j.Log4j2;
import net.forlevity.homeglue.entity.ApplianceDetector;
import net.forlevity.homeglue.entity.Device;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Use device configuration and recent power meter reading(s) to determine whether an "appliance" attached to a
 * device is on or off.
 */
@Singleton
@Log4j2
public class ApplianceStateDecider {

    private final Map<String, Instant> lastOverThresholdByDevice = new ConcurrentHashMap<>();

    /**
     * Determine whether an appliance is on, given its meter's latest power meter reading.
     *
     * @param applianceDetector device appliance detector configuration (must be inflated or session active)
     * @param watts latest watts
     * @return true if appliance is on
     */
    public boolean applianceOn(ApplianceDetector applianceDetector, Double watts) {
        Preconditions.checkNotNull(applianceDetector);
        Device device = applianceDetector.getDevice();
        if (device == null || device.getDeviceId() == null) {
            throw new IllegalArgumentException("ApplianceDetector must be attached to a valid device before use");
        }
        Instant now = Instant.now();
        boolean on;
        if (watts >= applianceDetector.getMinWatts()) {
            lastOverThresholdByDevice.put(device.getDeviceId(), now);
            // if it's over the threshold it's definitely on
            on = true;
        } else {
            Instant lastOverThreshold = lastOverThresholdByDevice.get(device.getDeviceId());
            if (lastOverThreshold == null) {
                on = false; // can't remember it ever being over threshold (at least since we started up)
            } else {
                // whether it's considered on depends on how much time has elapsed since it was last over threshold
                on = lastOverThreshold.plusSeconds(applianceDetector.getOffDelaySecs()).isAfter(now);
            }
        }
        return on;
    }
}
