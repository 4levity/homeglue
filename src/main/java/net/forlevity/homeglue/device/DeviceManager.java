/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.device;

import com.google.common.util.concurrent.Service;

import java.util.Collection;

/**
 * A service that handles a particular type/group of devices.
 */
public interface DeviceManager extends Service {

    /**
     * Return list of devices managed by this manager.
     * @return devices
     */
    Collection<DeviceConnector> getDevices();
}
