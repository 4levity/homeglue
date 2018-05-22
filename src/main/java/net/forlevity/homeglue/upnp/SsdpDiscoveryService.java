/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.upnp;

import com.google.common.util.concurrent.Service;
import com.google.inject.ImplementedBy;
import io.resourcepool.ssdp.model.SsdpService;

import java.util.Queue;
import java.util.function.Predicate;

@ImplementedBy(SsdpDiscoveryServiceImpl.class)
public interface SsdpDiscoveryService extends Service {

    void registerSsdp(Predicate<SsdpService> serviceMatch, Queue<SsdpService> newServices, int priority);
}
