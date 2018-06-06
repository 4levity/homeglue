/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.persistence;

import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.ServiceManager;
import net.forlevity.homeglue.device.DeviceState;
import net.forlevity.homeglue.entity.Device;
import net.forlevity.homeglue.testing.IntegrationTests;
import org.hibernate.Session;
import org.junit.Test;

import java.util.Collections;
import java.util.function.Function;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PersistenceTest extends IntegrationTests {

    @Test
    public void testPersistenceStartsCleanAndWorks() {
        ServiceManager serviceManager = new ServiceManager(Collections.singleton(persistence));
        serviceManager.startAsync().awaitHealthy();
        assertTrue(persistence.isRunning());
        String detectionId = "mydevice";
        Function<Session, Boolean> deviceLoader = session -> null != session.bySimpleNaturalId(Device.class).load(detectionId);

        assertFalse(persistence.exec(deviceLoader)); // no device
        persistence.exec(session -> { // create device
            session.saveOrUpdate(Device.from(new DeviceState(detectionId, true, ImmutableMap.of())));
            return null;
        });
        assertTrue(persistence.exec(deviceLoader)); // device exists

        serviceManager.stopAsync().awaitStopped();
    }

    @Test
    public void testDatabaseCleanBetweenTests() {
        testPersistenceStartsCleanAndWorks();
        // one of these tests fails if the database is not erased between tests
    }

    // TODO: test error handling: exception in work unit, exception while committing
}