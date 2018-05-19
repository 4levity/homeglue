package net.forlevity.homeglue;

import lombok.extern.log4j.Log4j2;
import net.forlevity.homeglue.device.PowerMeterConnector;
import net.forlevity.homeglue.device.WemoInsightConnector;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by ivan on 5/18/18.
 */
@Log4j2
public class Main {

    public static void main(String... args) throws Exception {
        log.info("starting");
        PowerMeterConnector meter = new WemoInsightConnector("wemo");
        meter.connect();
        new Timer().schedule(new TimerTask() {
                                 @Override
                                 public void run() {
                                     log.info("reading meter: {}", meter.read());
                                 }
                             }, 0, 5000);
    }
}
