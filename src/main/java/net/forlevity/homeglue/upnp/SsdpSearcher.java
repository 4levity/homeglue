package net.forlevity.homeglue.upnp;

import com.google.inject.ImplementedBy;
import io.resourcepool.ssdp.model.DiscoveryRequest;
import io.resourcepool.ssdp.model.SsdpService;

import java.util.function.Consumer;

@ImplementedBy(SsdpSearcherImpl.class)
public interface SsdpSearcher {

    BackgroundProcess startDiscovery(DiscoveryRequest discoveryRequest, Consumer<SsdpService> serviceConsumer);
}
