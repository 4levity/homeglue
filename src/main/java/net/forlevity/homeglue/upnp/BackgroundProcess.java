/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.upnp;

/**
 * A handle to a background process that can be stopped.
 */
public interface BackgroundProcess {

    /**
     * Stop the process.
     */
    void stop();
}
