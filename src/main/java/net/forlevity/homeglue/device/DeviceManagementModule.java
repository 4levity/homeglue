package net.forlevity.homeglue.device;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.Multibinder;
import net.forlevity.homeglue.device.generic_upnp.GenericUpnpConnectorFactory;
import net.forlevity.homeglue.device.wemo.WemoInsightConnectorFactory;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.Modifier;

public class DeviceManagementModule extends AbstractModule {

    private final Reflections reflections = new Reflections(new ConfigurationBuilder()
            .forPackages(this.getClass().getPackage().getName())); // i.e. net.4levity.homeglue.device

    @Override
    protected void configure() {

        // add all device managers
        Multibinder<DeviceManager> deviceManagerBinder = Multibinder.newSetBinder(binder(), DeviceManager.class);
        reflections.getSubTypesOf(DeviceManager.class).stream()
                .filter(clazz -> !clazz.isInterface() && !Modifier.isAbstract( clazz.getModifiers()))
                .forEach(clazz -> deviceManagerBinder.addBinding().to(clazz));

        // assisted injection for connectors
        install(new FactoryModuleBuilder().build(WemoInsightConnectorFactory.class));
        install(new FactoryModuleBuilder().build(GenericUpnpConnectorFactory.class));
    }
}
