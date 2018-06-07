/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.device;

import java.time.Duration;

/**
 * Provides a read-only view on an instance of DeviceConnector (not including management methods).
 */
public interface DeviceConnectorInfo extends DeviceInfo {

    /**
     * Return how much time needs to have elapsed since status was received from this device, before
     * it will be marked offline.
     *
     * @return duration
     */
    Duration getOfflineDelay();
}
