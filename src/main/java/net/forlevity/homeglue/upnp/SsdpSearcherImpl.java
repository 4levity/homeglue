package net.forlevity.homeglue.upnp;

import com.google.inject.Singleton;
import io.resourcepool.ssdp.client.SsdpClient;
import io.resourcepool.ssdp.model.DiscoveryListener;
import io.resourcepool.ssdp.model.DiscoveryRequest;
import io.resourcepool.ssdp.model.SsdpService;
import io.resourcepool.ssdp.model.SsdpServiceAnnouncement;
import lombok.extern.log4j.Log4j2;

import java.util.function.Consumer;

@Log4j2
@Singleton
public class SsdpSearcherImpl implements SsdpSearcher {

    public BackgroundProcess startDiscovery(DiscoveryRequest discoveryRequest, Consumer<SsdpService> serviceConsumer) {
        SsdpClient client = SsdpClient.create();
        client.discoverServices(discoveryRequest, new DiscoveryListener() {

            @Override
            public void onServiceDiscovered(SsdpService service) {
                log.trace("onServiceDiscovered: {}", service);
                serviceConsumer.accept(service);
            }

            @Override
            public void onServiceAnnouncement(SsdpServiceAnnouncement announcement) {
                log.debug("onServiceAnnouncement: {}", announcement);
            }

            @Override
            public void onFailed(Exception e) {
                log.warn("unexpected failure in SSDP discovery", e);
            }
        });
        return () -> client.stopDiscovery(); // return handle that allows stopping discovery
    }
}