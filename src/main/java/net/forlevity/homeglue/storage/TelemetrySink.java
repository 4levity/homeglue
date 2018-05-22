/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.storage;

import net.forlevity.homeglue.device.PowerMeterData;

/**
 * Somewhere to put telemetry information (e.g. instantaneous power from a meter).
 */
public interface TelemetrySink {

    /**
     * Take some new telemetry data and do something with it, like storing or logging it.
     * This should be called whenever new telemetry is available from a device.
     * @param deviceId device identifier
     * @param data telemetry
     */
    void accept(String deviceId, PowerMeterData data);
}
