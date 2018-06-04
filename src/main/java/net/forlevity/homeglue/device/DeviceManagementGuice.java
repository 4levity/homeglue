/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.device;

import com.google.common.util.concurrent.Service;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.Multibinder;
import net.forlevity.homeglue.device.generic_upnp.GenericUpnpConnectorFactory;
import net.forlevity.homeglue.device.generic_upnp.GenericUpnpManagerService;
import net.forlevity.homeglue.device.wemo.WemoInsightConnectorFactory;
import net.forlevity.homeglue.device.wemo.WemoInsightManagerService;

import java.util.function.Consumer;

/**
 * Child module for device managers and connectors.
 */
public class DeviceManagementGuice extends AbstractModule {

    @Override
    protected void configure() {
        // device managers
        Multibinder<Service> deviceManagerBinder = Multibinder.newSetBinder(binder(), Service.class);
        deviceManagerBinder.addBinding().to(WemoInsightManagerService.class);
        deviceManagerBinder.addBinding().to(GenericUpnpManagerService.class);

        // assisted injection for connectors
        install(new FactoryModuleBuilder().build(WemoInsightConnectorFactory.class));
        install(new FactoryModuleBuilder().build(GenericUpnpConnectorFactory.class));

        // device state processor
        bind(new TypeLiteral<Consumer<DeviceState>>(){}).to(DeviceStateProcessorService.class);

        // dispatcher
        bind(DeviceCommandDispatcher.class);

        // appliance detector
        bind(ApplianceStateDecider.class);
    }
}
