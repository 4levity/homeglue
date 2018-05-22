/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.upnp;

import com.google.common.util.concurrent.ServiceManager;
import net.forlevity.homeglue.HomeglueTests;
import org.junit.Test;

import java.util.Collections;

import static org.mockito.Mockito.mock;

public class SsdpDiscoveryServiceTest extends HomeglueTests {

    ServiceManager serviceManager;
    private SsdpSearcher searcher;

    @Test
    public void test() {

    }

    private TestSsdpDiscoveryServiceImpl startedService() {
        searcher = mock(SsdpSearcher.class);
        TestSsdpDiscoveryServiceImpl service = new TestSsdpDiscoveryServiceImpl(searcher);
        serviceManager = new ServiceManager(Collections.singleton(service));
        serviceManager.startAsync().awaitHealthy();
        return service;
    }

    private static class TestSsdpDiscoveryServiceImpl extends SsdpDiscoveryServiceImpl {

        public TestSsdpDiscoveryServiceImpl(SsdpSearcher ssdpSearcher) {
            super(ssdpSearcher, 100, 40, 0, 0);
        }

        void stop() throws Exception {
            shutDown();
        }
    }
}
