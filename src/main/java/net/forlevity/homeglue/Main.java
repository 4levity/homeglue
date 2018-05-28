/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue;

import com.google.inject.Guice;
import lombok.extern.log4j.Log4j2;
import net.forlevity.homeglue.util.PropertiesHelper;
import net.forlevity.homeglue.util.ResourceHelper;

import java.util.Properties;

/**
 * Application entry point.
 */
@Log4j2
public class Main {

    public static void main(String... args) {
        Properties configuration = ResourceHelper.resourceAsProperties("default.properties");
        PropertiesHelper.tryMergeFile(configuration, "homeglue.properties");
        Guice.createInjector(new ApplicationGuice(configuration)).getInstance(HomeglueApplication.class).start();
    }
}
