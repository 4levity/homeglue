/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.device;

import java.util.Map;

/**
 * Getters that represent state of a device, either in live device instances or records of device state.
 */
interface DeviceInfo {

    String DEVICE_ID_UNKNOWN = "unknown";

    /**
     * Get the unique identifier of this device, which might be something like its MAC address or serial number.
     * If the device ID equals DeviceConnector.DEVICE_ID_UNKNOWN , the device's identity has not been determined.
     *
     * @return device id
     */
    String getDeviceId();

    /**
     * True if the device is "probably online."
     *
     * @return true if device is or was recently contactable
     */
    boolean isConnected();

    /**
     * Get metadata about device, such as make/model/serial or whatever else is available for that device.
     *
     * @return copy of device metadata
     */
    Map<String,String> getDeviceDetails();

    /**
     * If the device is a meter, get watts that are being consumed by loads. If measuring generated energy,
     * the number should be negative.
     *
     * @return watts
     */
    default Double getInstantaneousWatts() {
        return null;
    }
}
