/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.device;

import com.google.common.util.concurrent.Service;
import com.google.inject.ImplementedBy;

import java.util.function.Consumer;

/**
 * Handle all device state from device managers. Manage persistent device list and last-known connected/relay/switch
 * state information. Generate state change events.
 */
@ImplementedBy(DeviceStateProcessorServiceImpl.class)
public interface DeviceStateProcessorService extends Service, Consumer<DeviceState> {

    /**
     * Process device status. If any changes, generate events and update db. Runs serially on queue processing thread.
     *
     * @param newDeviceState device status
     */
    void handle(DeviceState newDeviceState);

    /**
     * Get the last reported state (since service startup) for the given device detection ID, or null if none.
     *
     * @param deviceDetectionId detectionId
     * @return last known DeviceState or null
     */
    DeviceState getLastState(String deviceDetectionId);
}
