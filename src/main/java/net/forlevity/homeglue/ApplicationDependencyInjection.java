/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue;

import com.google.inject.AbstractModule;
import net.forlevity.homeglue.device.DeviceManagementDependencyInjection;
import net.forlevity.homeglue.storage.DeviceStatusSink;
import net.forlevity.homeglue.storage.NoStorage;
import net.forlevity.homeglue.storage.TelemetrySink;

/**
 * Top level dependency injection module.
 */
public class ApplicationDependencyInjection extends AbstractModule {

    @Override
    protected void configure() {
        // the application
        bind(HomeglueApplication.class);

        // device manager child module
        install(new DeviceManagementDependencyInjection());

        // storage
        bind(DeviceStatusSink.class).to(NoStorage.class);
        bind(TelemetrySink.class).to(NoStorage.class);
    }
}
