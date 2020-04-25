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
import net.forlevity.homeglue.testing.HomeglueTests;
import net.forlevity.homeglue.testing.IntegrationTests;
import net.forlevity.homeglue.testing.ThisTestException;
import org.hibernate.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

public class PersistenceIntegrationTest extends IntegrationTests {

    ServiceManager serviceManager;

    @BeforeEach
    public void startPersistence() {
        // fresh persistence service instance is created every run
        serviceManager = new ServiceManager(Collections.singleton(persistence));
        serviceManager.startAsync().awaitHealthy();
        assertTrue(persistence.isRunning());
    }

    @AfterEach
    public void stopPersistence() {
        serviceManager.stopAsync().awaitStopped();
        assertFalse(persistence.isRunning());
    }

    @Test
    public void testPersistenceStartsCleanAndWorks() {
        String detectionId = "mydevice";
        Function<Session, Boolean> deviceLoader = session -> null != session.bySimpleNaturalId(Device.class).load(detectionId);

        assertFalse(persistence.exec(deviceLoader)); // no device
        persistence.exec(session -> { // create device
            session.saveOrUpdate(Device.from(new DeviceState(detectionId, ImmutableMap.of())));
            return null;
        });
        assertTrue(persistence.exec(deviceLoader)); // device exists
    }

    @Test
    public void throwsThrough() {
        assertThrows(ThisTestException.class, () -> persistence.exec(HomeglueTests::throwThisTestException));
    }

    @Test
    public void testDatabaseCleanBetweenTests() {
        testPersistenceStartsCleanAndWorks();
        // one of these tests fails if the database is not erased between tests
    }

    // TODO: test error handling: exception in work unit, exception while committing
}