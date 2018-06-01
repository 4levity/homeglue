/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue;

import com.google.common.util.concurrent.Service;
import net.forlevity.homeglue.device.DeviceStateProcessorService;
import net.forlevity.homeglue.device.generic_upnp.GenericUpnpManagerService;
import net.forlevity.homeglue.device.wemo.WemoInsightManagerService;
import net.forlevity.homeglue.http.SimpleHttpClient;
import net.forlevity.homeglue.persistence.PersistenceService;
import net.forlevity.homeglue.sim.SimulatedNetwork;
import net.forlevity.homeglue.sink.IftttDeviceEventService;
import net.forlevity.homeglue.testing.IntegrationTests;
import net.forlevity.homeglue.upnp.SsdpDiscoveryService;
import net.forlevity.homeglue.upnp.SsdpSearcher;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ApplicationTest extends IntegrationTests {

    @Test
    public void startStopApplication() {
        application.start();
        assertTrue(application.getServiceManager().isHealthy());
        checkService(WemoInsightManagerService.class);
        checkService(GenericUpnpManagerService.class);
        checkService(PersistenceService.class);
        checkService(DeviceStateProcessorService.class);
        checkService(SsdpDiscoveryService.class);
        checkService(IftttDeviceEventService.class);
        assertTrue(injector.getInstance(SimpleHttpClient.class) instanceof SimulatedNetwork);
        assertTrue(injector.getInstance(SsdpSearcher.class) instanceof SimulatedNetwork);
        application.stop();
        assertFalse(injector.getInstance(WemoInsightManagerService.class).isRunning());
        assertFalse(injector.getInstance(GenericUpnpManagerService.class).isRunning());
        assertFalse(injector.getInstance(SsdpDiscoveryService.class).isRunning());
    }

    private void checkService(Class<? extends Service> serviceClass) {
        // this checks that the service started, and also that it is a singleton
        // (if not, we'd get a new instance that wouldn't be running)
        assertTrue(injector.getInstance(serviceClass).isRunning());
    }
}
