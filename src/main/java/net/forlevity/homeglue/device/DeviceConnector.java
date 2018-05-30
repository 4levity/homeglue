/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.device;

/**
 * Represents a connection to a device like a meter/switch/appliance/etc.
 */
public interface DeviceConnector extends DeviceStatus {

    /**
     * Attempt to make a connection to the device and retrieve metadata about it.
     * @return true if successfully connected to this device and got latest metadata
     */
    boolean connect();
}
