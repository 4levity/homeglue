/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.testing;

import com.google.inject.Guice;
import com.google.inject.Injector;
import net.forlevity.homeglue.ApplicationGuice;
import net.forlevity.homeglue.HomeglueApplication;
import net.forlevity.homeglue.persistence.PersistenceService;
import net.forlevity.homeglue.util.Json;
import net.forlevity.homeglue.util.ResourceHelper;
import org.junit.After;
import org.junit.Before;

import java.util.Properties;

public class IntegrationTests extends HomeglueTests {

    protected Injector injector;
    protected PersistenceService persistence;
    protected HomeglueApplication application;

    @Before
    public void prepareApplication() {
        makeTestInjector();
        application = injector.getInstance(HomeglueApplication.class);
        persistence = injector.getInstance(PersistenceService.class);
        json = injector.getInstance(Json.class);
    }

    protected void makeTestInjector() {
        Properties configuration = ResourceHelper.resourceAsProperties("default.properties", "test.properties");
        injector = Guice.createInjector(new ApplicationGuice(configuration));
    }

    @After
    public void stopApplication() {
        if (!application.isStopped()) {
            application.stop();
        }
        // TODO: erase contents of in-memory database
    }
}
