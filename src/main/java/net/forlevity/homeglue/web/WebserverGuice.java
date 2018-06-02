/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.web;

import org.jboss.resteasy.plugins.guice.ext.RequestScopeModule;

public class WebserverGuice extends RequestScopeModule {

    @Override
    protected void configure() {
        super.configure();
        bind(WebserverService.class);
        bind(ObjectMapperProvider.class);
        bind(RootResource.class);
    }
}
