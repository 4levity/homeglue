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
