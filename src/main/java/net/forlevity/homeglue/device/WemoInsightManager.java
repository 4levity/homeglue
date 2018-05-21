package net.forlevity.homeglue.device;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.resourcepool.ssdp.client.SsdpClient;
import io.resourcepool.ssdp.model.DiscoveryListener;
import io.resourcepool.ssdp.model.DiscoveryRequest;
import io.resourcepool.ssdp.model.SsdpService;
import io.resourcepool.ssdp.model.SsdpServiceAnnouncement;
import lombok.extern.log4j.Log4j2;
import net.forlevity.homeglue.storage.DeviceStatusSink;
import net.forlevity.homeglue.storage.TelemetrySink;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log4j2
@Singleton
public class WemoInsightManager extends AbstractDeviceManager {

    private static final long SCAN_PERIOD_MILLIS = 5000;
    private static final Pattern SSDP_SERIALNUMBER = Pattern.compile("uuid:Insight-1.*");
    private static final Pattern SSDP_LOCATION = Pattern.compile(
            "http://(?<ipAddress>[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}):(?<port>[0-9]{5})/setup.xml");
    private static final long SSDP_DISCOVERY_WAIT_MILLIS = 2000;

    private final TelemetrySink telemetrySink;
    private final Map<String, WemoInsightConnector> insights = new HashMap<>();
    private final Timer timer = new Timer();

    @Inject
    public WemoInsightManager(DeviceStatusSink deviceStatusSink, TelemetrySink telemetrySink) {
        super(deviceStatusSink);
        this.telemetrySink = telemetrySink;
    }

    @Override
    public void startUp() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                update();
            }
        }, 0, SCAN_PERIOD_MILLIS);
    }

    @Override
    protected void shutDown() {
        timer.cancel();
    }

    private void update() {
        if (getDevices().isEmpty()) {
            discover().forEach(meter -> {
                meter.connect();
                insights.put(meter.getDeviceDetails().get("macAddress"), meter);
                register(meter);
            });
        }
        insights.values().forEach(meter -> telemetrySink.accept(meter.getDeviceId(), meter.read()));
    }

    private Collection<WemoInsightConnector> discover() {
        Map<String, WemoInsightConnector> meters = new HashMap<>();
        SsdpClient client = SsdpClient.create();
        DiscoveryRequest wemoInsightDiscoveryStrategy = DiscoveryRequest.discoverRootDevice();
        client.discoverServices(wemoInsightDiscoveryStrategy, new DiscoveryListener() {

            @Override
            public void onServiceDiscovered(SsdpService service) {
                log.debug("onServiceDiscovered: {}", service);
                if (SSDP_SERIALNUMBER.matcher(service.getSerialNumber()).matches()) {
                    Matcher location = SSDP_LOCATION.matcher(service.getLocation());
                    if (location.matches()) {
                        String ipAddress = location.group("ipAddress");
                        int port = Integer.valueOf(location.group("port"));
                        synchronized (meters) {
                            if (!meters.containsKey(ipAddress)) {
                                log.info("found Insight meter at {}:{}", ipAddress, port);
                            }
                            meters.put(ipAddress, new WemoInsightConnector(ipAddress, port));
                        }
                    } else {
                        log.warn("looks like Insight but has unexpected location: {}", service.getLocation());
                    }
                }
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
        try {
            Thread.sleep(SSDP_DISCOVERY_WAIT_MILLIS);
        } catch (InterruptedException e) {
            Thread.interrupted();
        }
        client.stopDiscovery();
        return meters.values();
    }
}
