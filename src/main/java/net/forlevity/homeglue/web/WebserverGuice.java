/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.web;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import org.jboss.resteasy.plugins.guice.ext.RequestScopeModule;

import javax.ws.rs.ext.Provider;

/**
 * Guice module for RESTeasy w/ Guice injection support. Automatically binds @Provider annotated resources under a
 * given package name prefix.
 */
public abstract class WebserverGuice extends RequestScopeModule {

    private final String resourcePackagePrefix;

    protected WebserverGuice(String resourcePackagePrefix) {
        this.resourcePackagePrefix = resourcePackagePrefix;
    }

    @Override
    protected final void configure() {
        super.configure();
        bind(WebserverService.class);
        bind(ObjectMapperProvider.class);

        ScanResult scanResult = new ClassGraph()
                .enableClassInfo()
                .enableAnnotationInfo()
                .whitelistPackages(resourcePackagePrefix)
                .scan();
        ClassInfoList resources = scanResult.getClassesWithAnnotation(Provider.class.getName());
        resources.forEach(classInfo -> bind(classInfo.loadClass()));

        configureMore();
    }

    protected abstract void configureMore();
}
