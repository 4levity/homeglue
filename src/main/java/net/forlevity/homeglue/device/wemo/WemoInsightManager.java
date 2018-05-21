package net.forlevity.homeglue.device.wemo;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.resourcepool.ssdp.model.DiscoveryRequest;
import lombok.extern.log4j.Log4j2;
import net.forlevity.homeglue.device.AbstractDeviceManager;
import net.forlevity.homeglue.storage.DeviceStatusSink;
import net.forlevity.homeglue.storage.TelemetrySink;
import net.forlevity.homeglue.upnp.BackgroundProcess;
import net.forlevity.homeglue.upnp.SsdpServiceFinder;

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

    private final SsdpServiceFinder ssdpServiceFinder;
    private final WemoInsightConnectorFactory connectorFactory;
    private final TelemetrySink telemetrySink;
    private final Map<String, WemoInsightConnector> insights = new HashMap<>();
    private final Timer timer = new Timer();

    @Inject
    public WemoInsightManager(SsdpServiceFinder ssdpServiceFinder,
                              WemoInsightConnectorFactory connectorFactory,
                              DeviceStatusSink deviceStatusSink, TelemetrySink telemetrySink) {
        super(deviceStatusSink);
        this.ssdpServiceFinder = ssdpServiceFinder;
        this.connectorFactory = connectorFactory;
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
                if (meter.connect()) {
                    insights.put(meter.getDeviceDetails().get("macAddress"), meter);
                    register(meter);
                }
            });
        }
        insights.values().forEach(meter -> telemetrySink.accept(meter.getDeviceId(), meter.read()));
    }

    private Collection<WemoInsightConnector> discover() {
        Map<String, WemoInsightConnector> meters = new HashMap<>();
        DiscoveryRequest wemoInsightDiscoveryStrategy = DiscoveryRequest.discoverRootDevice();
        BackgroundProcess discovery = ssdpServiceFinder.startDiscovery(wemoInsightDiscoveryStrategy, service -> {
            if (SSDP_SERIALNUMBER.matcher(service.getSerialNumber()).matches()) {
                Matcher location = SSDP_LOCATION.matcher(service.getLocation());
                if (location.matches()) {
                    String ipAddress = location.group("ipAddress");
                    int port = Integer.valueOf(location.group("port"));
                    synchronized (meters) {
                        if (!meters.containsKey(ipAddress)) {
                            log.info("found Insight meter at {}:{}", ipAddress, port);
                        }
                        WemoInsightConnector newConnector = connectorFactory.create(ipAddress, port);
                        meters.put(ipAddress, newConnector);
                    }
                } else {
                    log.warn("looks like Insight but has unexpected location: {}", service.getLocation());
                }
            }
        });
        try {
            Thread.sleep(SSDP_DISCOVERY_WAIT_MILLIS);
        } catch (InterruptedException e) {
            Thread.interrupted();
        }
        discovery.stop();
        return meters.values();
    }
}
