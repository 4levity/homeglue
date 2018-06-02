/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.web;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.github.lukehutch.fastclasspathscanner.scanner.ScanResult;
import lombok.AllArgsConstructor;
import org.jboss.resteasy.plugins.guice.ext.RequestScopeModule;

import javax.ws.rs.ext.Provider;
import java.util.List;

/**
 * Guice module for RESTeasy w/ Guice injection support. Automatically binds @Provider annotated resources under a
 * given package name prefix.
 */
@AllArgsConstructor
public class WebserverGuice extends RequestScopeModule {

    private final String resourcePackagePrefix;

    @Override
    protected void configure() {
        super.configure();
        bind(WebserverService.class);
        bind(ObjectMapperProvider.class);

        ScanResult scanResult = new FastClasspathScanner(resourcePackagePrefix).scan();
        List<String> resources = scanResult.getNamesOfClassesWithAnnotation(Provider.class);
        resources.forEach(className -> bind(scanResult.getClassNameToClassInfo().get(className).getClassRef()));
    }
}
