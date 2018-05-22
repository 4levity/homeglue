/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue;

import com.google.inject.AbstractModule;
import net.forlevity.homeglue.device.DeviceManagementModule;
import net.forlevity.homeglue.storage.DeviceStatusSink;
import net.forlevity.homeglue.storage.NoStorage;
import net.forlevity.homeglue.storage.TelemetrySink;

public class ApplicationModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Application.class);

        install(new DeviceManagementModule());

        // storage
        bind(DeviceStatusSink.class).to(NoStorage.class);
        bind(TelemetrySink.class).to(NoStorage.class);
    }
}
