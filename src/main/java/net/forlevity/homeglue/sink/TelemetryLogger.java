/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.sink;

import com.google.inject.Singleton;
import lombok.extern.log4j.Log4j2;
import net.forlevity.homeglue.device.PowerMeterData;

import java.util.function.Consumer;

@Log4j2
@Singleton
public class TelemetryLogger implements Consumer<PowerMeterData> {

    @Override
    public void accept(PowerMeterData powerMeterData) {
        log.info("{}", powerMeterData);
    }
}
