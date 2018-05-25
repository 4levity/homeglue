/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue;

import com.google.inject.Guice;
import com.google.inject.Injector;
import net.forlevity.homeglue.device.generic_upnp.GenericUpnpManager;
import net.forlevity.homeglue.device.wemo.WemoInsightManager;
import net.forlevity.homeglue.http.SimpleHttpClient;
import net.forlevity.homeglue.sim.SimulatedNetwork;
import net.forlevity.homeglue.testing.HomeglueTests;
import net.forlevity.homeglue.upnp.SsdpDiscoveryService;
import net.forlevity.homeglue.upnp.SsdpSearcher;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ApplicationTest extends HomeglueTests {

    @Test
    public void startStopApplication() throws IOException {
        Properties configuration = new Properties();
        configuration.load(Main.class.getResourceAsStream("/test.homeglue.properties"));
        Injector injector = Guice.createInjector(new ApplicationDependencyInjection(configuration));
        HomeglueApplication application = injector.getInstance(HomeglueApplication.class);
        application.start();
        assertTrue(application.getServiceManager().isHealthy());
        assertTrue(injector.getInstance(WemoInsightManager.class).isRunning());
        assertTrue(injector.getInstance(GenericUpnpManager.class).isRunning());
        assertTrue(injector.getInstance(SsdpDiscoveryService.class).isRunning());
        assertTrue(injector.getInstance(SimpleHttpClient.class) instanceof SimulatedNetwork);
        assertTrue(injector.getInstance(SsdpSearcher.class) instanceof SimulatedNetwork);
        application.stop();
        assertFalse(injector.getInstance(WemoInsightManager.class).isRunning());
        assertFalse(injector.getInstance(GenericUpnpManager.class).isRunning());
        assertFalse(injector.getInstance(SsdpDiscoveryService.class).isRunning());
    }
}
