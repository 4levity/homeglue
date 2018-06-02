/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.api;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class ApiGuice extends AbstractModule {

    @Override
    protected void configure() {
        install(new FactoryModuleBuilder().build(DeviceResource.Factory.class));
    }
}
