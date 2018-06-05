/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.api;

import com.google.inject.assistedinject.FactoryModuleBuilder;
import net.forlevity.homeglue.web.WebserverGuice;

public class ApiGuice extends WebserverGuice {

    public ApiGuice() {
        super("net.forlevity.homeglue.api");
    }

    @Override
    protected void configureMore() {
        install(new FactoryModuleBuilder().build(DeviceResource.Factory.class));
        install(new FactoryModuleBuilder().build(RelayResource.Factory.class));
    }
}
