package net.forlevity.homeglue.device.wemo;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.resourcepool.ssdp.model.SsdpService;
import lombok.extern.log4j.Log4j2;
import net.forlevity.homeglue.device.AbstractDeviceManager;
import net.forlevity.homeglue.device.PowerMeterConnector;
import net.forlevity.homeglue.device.PowerMeterData;
import net.forlevity.homeglue.storage.DeviceStatusSink;
import net.forlevity.homeglue.storage.TelemetrySink;
import net.forlevity.homeglue.upnp.SsdpDiscoveryService;

import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log4j2
@Singleton
public class WemoInsightManager extends AbstractDeviceManager {

    private static final Pattern SSDP_SERIALNUMBER = Pattern.compile("uuid:Insight-1.*");
    private static final Pattern SSDP_LOCATION = Pattern.compile(
            "http://(?<ipAddress>[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}):(?<port>[0-9]{5})/setup.xml");
    private static final long SCAN_PERIOD_MILLIS = 5000L;

    private final SsdpDiscoveryService ssdpDiscoveryService;
    private final WemoInsightConnectorFactory connectorFactory;
    private final TelemetrySink telemetrySink;
    private final ConcurrentHashMap<String, WemoInsightConnector> insights = new ConcurrentHashMap<>();
    private final ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1);
    private final LinkedBlockingQueue<SsdpService> discoveredWemo = new LinkedBlockingQueue<>();

    @Inject
    public WemoInsightManager(SsdpDiscoveryService ssdpDiscoveryService,
                              WemoInsightConnectorFactory connectorFactory,
                              DeviceStatusSink deviceStatusSink, TelemetrySink telemetrySink) {
        super(deviceStatusSink);
        this.ssdpDiscoveryService = ssdpDiscoveryService;
        this.connectorFactory = connectorFactory;
        this.telemetrySink = telemetrySink;
    }

    @Override
    protected void run() throws Exception {
        // register our queue for devices that look like wemo insights
        ssdpDiscoveryService.registerSsdp(service ->
                (SSDP_SERIALNUMBER.matcher(service.getSerialNumber()).matches()
                        && SSDP_LOCATION.matcher(service.getLocation()).matches()), discoveredWemo, 1);

        mainLoop();
    }

    private void mainLoop() throws InterruptedException {
        // whenever a new wemo is discovered, add it to our list and try to connect
        while (true) {
            SsdpService wemo = discoveredWemo.take(); // on interrupted, service will quit
            try {
                handleWemoDiscovery(wemo);
            } catch (RuntimeException e) {
                log.error("unexpected exception handling WeMo discovery (continuing)", e);
            }
        }
    }

    private void handleWemoDiscovery(SsdpService wemo) {
        Matcher location = SSDP_LOCATION.matcher(wemo.getLocation());
        if (location.matches()) {
            String ipAddress = location.group("ipAddress");
            int port = Integer.valueOf(location.group("port"));
            handleWemoDiscovery(ipAddress, port);
        } else {
            log.warn("thought we found Insight meter but has unexpected location: {}", wemo.getLocation());
        }
    }

    private void handleWemoDiscovery(String ipAddress, int port) {
        WemoInsightConnector foundConnector = insights.get(ipAddress);
        if (foundConnector == null) {
            WemoInsightConnector newConnector = connectorFactory.create(ipAddress, port);
            if (newConnector.connect()) {
                log.info("connected to Insight meter at {}:{}", ipAddress, port);
                boolean firstDevice = insights.isEmpty();
                insights.put(ipAddress, newConnector);
                register(newConnector);
                if (firstDevice) {
                    // as soon as the first device is found, start polling
                    startTimer();
                }
            } else {
                log.warn("detected but failed to connect to Insight meter at {}:{}", ipAddress, port);
            }
        } else if (foundConnector.getPort() != port) {
            log.info("WeMo Insight port at {} changed from {} to {}",
                    ipAddress, foundConnector.getPort(), port);
            foundConnector.setPort(port);
        }
    }

    private void startTimer() {
        executor.scheduleAtFixedRate(() -> poll(), 0, SCAN_PERIOD_MILLIS, TimeUnit.MILLISECONDS);
    }

    private void poll() {
        insights.values().forEach(this::poll);
    }

    private void poll(PowerMeterConnector meter) {
        try {
            PowerMeterData read = meter.read();
            // TODO: handle failure to read meter
            telemetrySink.accept(meter.getDeviceId(), read);
        } catch(RuntimeException e) {
            log.error("unexpected exception during poll of {} (continuing)", meter, e);
        }
    }

    @Override
    protected void shutDown() {
        executor.shutdown();
    }
}
