/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.util;

import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.Service;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.Map;

/**
 * Service dependency manager. Injector provides this to services that need to wait on other services, although they
 * may not have a direct link to those services - e.g. WebserverService does not use or know about PersistenceService,
 * however the resources it serves might, and if so it should not serve pages until after persistence is running.
W */
@Log4j2
public class ServiceDependencies {

    public final static ServiceDependencies NONE = new ServiceDependencies(ImmutableMap.of());

    private final Map<Class<? extends Service>, List<Service>> dependsOn;

    /**
     * Create a new ServiceDependencies object. Typically call from Guice @Provides injector method.
     *
     * @param dependsOn dependency graph
     */
    public ServiceDependencies(Map<Class<? extends Service>, List<Service>> dependsOn) {
        this.dependsOn = dependsOn;
    }

    /**
     * Block until dependency services are running, if any. Typically call from service startUp() method.
     *
     * @param forService service that has dependencies (typically "this")
     */
    public void waitForDependencies(Service forService) {
        List<Service> match = null;
        for (Class<? extends Service> candidate : dependsOn.keySet()) {
            if (candidate.isAssignableFrom(forService.getClass())) {
                if (match == null) {
                    match = dependsOn.get(candidate);
                } else {
                    throw new IllegalStateException("ambiguous service dependency for "
                            + forService.getClass().getCanonicalName());
                }
            }
        }
        if (match != null) {
            match.forEach(dependency -> {
                if (!dependency.isRunning()) {
                    waitFor(dependency, forService);
                } else {
                    log.trace("{} doesn't need to wait for {}", forService, dependency);
                }
            });
        }
    }

    private void waitFor(Service dependency, Service forService) {
        log.debug("{} is waiting for {}", forService, dependency);
        try {
            dependency.awaitRunning();
        } catch (IllegalStateException e) {
            log.error("{} failed because: {}", forService.toString(), e.getMessage());
            throw e;
        }
    }
}
