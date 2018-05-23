/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.device.generic_upnp;

import net.forlevity.homeglue.upnp.SsdpServiceDefinition;

/**
 * Interface for assisted injection FactoryModuleBuilder.
 */
public interface GenericUpnpConnectorFactory {

    /**
     * Create a GenericUpnpConnector.
     * @param firstService a service for this device
     * @return connector
     */
    GenericUpnpConnector create(SsdpServiceDefinition firstService);
}
