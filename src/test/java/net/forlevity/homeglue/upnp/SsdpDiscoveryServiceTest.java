/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.upnp;

import com.google.common.util.concurrent.ServiceManager;
import net.forlevity.homeglue.HomeglueTests;
import net.forlevity.homeglue.sim.SimulatedNetwork;
import org.junit.Test;

import java.util.Collections;

public class SsdpDiscoveryServiceTest extends HomeglueTests {

    SimulatedNetwork network = new SimulatedNetwork();
    ServiceManager serviceManager;

    @Test
    public void testDiscoveryService() {
        SsdpDiscoveryService service = new SsdpDiscoveryServiceImpl(network,100,40,0,0);
        serviceManager = new ServiceManager(Collections.singleton(service));
    }
}
