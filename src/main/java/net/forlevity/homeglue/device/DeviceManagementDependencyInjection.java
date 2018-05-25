/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.device;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.Multibinder;
import net.forlevity.homeglue.device.generic_upnp.GenericUpnpConnectorFactory;
import net.forlevity.homeglue.device.generic_upnp.GenericUpnpManager;
import net.forlevity.homeglue.device.wemo.WemoInsightConnectorFactory;
import net.forlevity.homeglue.device.wemo.WemoInsightManager;

/**
 * Child module for device managers and connectors.
 */
public class DeviceManagementDependencyInjection extends AbstractModule {

    @Override
    protected void configure() {

        // add all device managers
        Multibinder<DeviceManager> deviceManagerBinder = Multibinder.newSetBinder(binder(), DeviceManager.class);
        deviceManagerBinder.addBinding().to(WemoInsightManager.class);
        deviceManagerBinder.addBinding().to(GenericUpnpManager.class);

        // assisted injection for connectors
        install(new FactoryModuleBuilder().build(WemoInsightConnectorFactory.class));
        install(new FactoryModuleBuilder().build(GenericUpnpConnectorFactory.class));
    }
}
