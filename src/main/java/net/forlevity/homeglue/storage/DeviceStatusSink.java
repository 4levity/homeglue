/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.storage;

import net.forlevity.homeglue.device.DeviceConnector;

/**
 * Somewhere to put device info and info data (e.g. connection state, serial number info, etc).
 */
public interface DeviceStatusSink {

    /**
     * Take some device info and do something with it, like storing or logging it.
     * This should be called whenever a new device is discovered or a change is detected.
     * @param deviceConnector the device info
     */
    void accept(DeviceConnector deviceConnector);
}
