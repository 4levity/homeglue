package net.forlevity.homeglue;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import net.forlevity.homeglue.device.DeviceManager;
import net.forlevity.homeglue.storage.DeviceStatusSink;
import net.forlevity.homeglue.storage.NoStorage;
import net.forlevity.homeglue.storage.TelemetrySink;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.Modifier;

public class ApplicationModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Application.class);

        // add all device managers
        Multibinder<DeviceManager> deviceManagerBinder = Multibinder.newSetBinder(binder(), DeviceManager.class);
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .forPackages(this.getClass().getPackage().getName())); // i.e. net.4levity.homeglue
        reflections.getSubTypesOf(DeviceManager.class).stream()
                .filter(clazz -> !clazz.isInterface() && !Modifier.isAbstract( clazz.getModifiers()))
                .forEach(clazz -> deviceManagerBinder.addBinding().to(clazz));

        // storage
        bind(DeviceStatusSink.class).to(NoStorage.class);
        bind(TelemetrySink.class).to(NoStorage.class);
    }
}
