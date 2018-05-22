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
