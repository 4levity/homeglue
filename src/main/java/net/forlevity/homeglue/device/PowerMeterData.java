package net.forlevity.homeglue.device;

import lombok.AllArgsConstructor;
import lombok.ToString;

import java.time.Instant;

@AllArgsConstructor
@ToString
public class PowerMeterData {

    private final Instant timestamp = Instant.now();

    private final double instantaneousWatts;
}
