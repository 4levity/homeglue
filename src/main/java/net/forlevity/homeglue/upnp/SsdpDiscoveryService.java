/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.upnp;

import com.google.common.util.concurrent.Service;
import com.google.inject.ImplementedBy;

import java.util.Queue;
import java.util.function.Predicate;

/**
 * Application service that periodically SSDP scans the network for UPnP services. Users of SsdpDiscoveryService may
 * register interest in particuar UPnP services and get notifications (via queue) on matching service discovery.
 */
@ImplementedBy(SsdpDiscoveryServiceImpl.class)
public interface SsdpDiscoveryService extends Service {

    /**
     * Request to asynchronously receive information about a particular service whenever it is discovered by SSDP.
     * If two registrations would both match the same service, the notification will go to whichever queue was
     * registered with a LOWER priority number. Multiple notifications are often sent for the same service.
     *
     * @param serviceMatch test for service match
     * @param newServices queue where matching services will be offered
     * @param priority what order to process this registration, compared to other registrations
     */
    void registerSsdp(Predicate<SsdpServiceDefinition> serviceMatch,
                      Queue<SsdpServiceDefinition> newServices,
                      int priority);
}
