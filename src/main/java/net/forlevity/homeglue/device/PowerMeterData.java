/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.device;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.time.Instant;

/**
 * Data from a power meter e.g. Belkin WeMo Insight.
 */
@AllArgsConstructor
@ToString
@Getter
public class PowerMeterData {

    private final Instant timestamp = Instant.now();

    private final double instantaneousWatts;
}
