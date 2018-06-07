/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.device;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * Represents a connection to a device like a meter/switch/appliance/etc.
 */
public interface DeviceConnector extends DeviceConnectorInfo {

    /**
     * Attempt to make a connection to the device and retrieve metadata about it, then start the operation of the
     * connector. May block for a little while. May create threads etc.
     *
     * @return true if successfully connected to this device and got latest metadata
     */
    boolean start();

    /**
     * Stop the connector. It may not be possible to start it again.
     */
    default void terminate() { }

    /**
     * Dispatch a command to the device and get a future to track the result.
     *
     * @param command command
     * @return result
     */
    default Future<Command.Result> dispatch(Command command) {
        return CompletableFuture.completedFuture(Command.Result.NOT_SUPPORTED);
    }
}
