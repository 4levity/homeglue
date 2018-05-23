/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.device.wemo;

/**
 * Interface for assisted injection FactoryModuleBuilder.
 */
public interface WemoInsightConnectorFactory {

    /**
     * Create an Insight connector.
     * @param hostAddress IP address or hostname
     * @param port TCP upnpPort
     * @return connector
     */
    WemoInsightConnector create(String hostAddress, int port);
}
