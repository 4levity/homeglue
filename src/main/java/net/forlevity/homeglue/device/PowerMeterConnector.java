/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.device;

import net.forlevity.homeglue.storage.PowerMeterData;

/**
 * A connection to a device that is a readable power meter, e.g. Belkin WeMo Insight.
 */
public interface PowerMeterConnector extends DeviceConnector {

    /**
     * Attempt to read the power meter.
     * @return data from meter, or null if failed to read
     */
    PowerMeterData read();
}
