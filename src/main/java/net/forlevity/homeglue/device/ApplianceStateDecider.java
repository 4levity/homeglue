/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.device;

import com.google.inject.Singleton;
import lombok.extern.log4j.Log4j2;
import net.forlevity.homeglue.entity.ApplianceDetector;

/**
 * Use device configuration and recent power meter reading(s) to determine whether an "appliance" attached to a
 * device is on or off.
 */
@Singleton
@Log4j2
public class ApplianceStateDecider {

    public boolean applianceOn(ApplianceDetector applianceDetector, Double watts) {
        if (applianceDetector.getOffDelaySecs() > 0) {
            log.warn("off delay set at device {}, not implemented", applianceDetector.getDevice().getDeviceId());
        }
        return watts >= applianceDetector.getMinWatts();
    }
}
