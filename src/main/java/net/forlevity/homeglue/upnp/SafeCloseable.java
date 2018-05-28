/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.upnp;

/**
 * A handle to a resource that can be closed such as a background process, where failure to close is unlikely.
 */
public interface SafeCloseable extends AutoCloseable {

    /**
     * Close the resource or stop the background process.
     */
    @Override
    void close();
}
