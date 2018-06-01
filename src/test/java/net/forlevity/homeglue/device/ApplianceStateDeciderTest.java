/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.device;

import net.forlevity.homeglue.entity.ApplianceDetector;
import net.forlevity.homeglue.testing.HomeglueTests;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ApplianceStateDeciderTest extends HomeglueTests {

    @Test
    public void applianceOn() {
        ApplianceStateDecider decider = new ApplianceStateDecider();
        ApplianceDetector applianceDetector = new ApplianceDetector().withDefaultSettings();
        assertFalse(decider.applianceOn(applianceDetector, 0.0));
        assertFalse(decider.applianceOn(applianceDetector, 1.0)); // default is 5w threshold
        assertTrue(decider.applianceOn(applianceDetector, 6.0));
        assertFalse(decider.applianceOn(applianceDetector, -1.0));

        applianceDetector.setMinWatts(0); // effectively always on
        assertTrue(decider.applianceOn(applianceDetector, 0.0));
        assertTrue(decider.applianceOn(applianceDetector, 0.001));

        applianceDetector.setMinWatts(50);
        assertFalse(decider.applianceOn(applianceDetector, 49.99));
        assertTrue(decider.applianceOn(applianceDetector, 50.01));
    }
}