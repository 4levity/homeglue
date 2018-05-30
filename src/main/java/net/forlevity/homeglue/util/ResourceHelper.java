/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.util;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import java.io.IOException;
import java.util.Properties;

/**
 * Static convenience methods related to loading data from resources.
 */
public class ResourceHelper {

    private ResourceHelper() {}

    /**
     * Get a resource by name and return its UTF-8 text contents as a string.
     *
     * @param resourceName name
     * @return text contents
     */
    public static String resourceAsString(String resourceName) {
        try {
            return Resources.toString(Resources.getResource(resourceName), Charsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Load one or more resource *.properties files by name. Files load in order (later items overwrite earlier).
     *
     * @param resourceNames names
     * @return properties
     */
    public static Properties resourceAsProperties(String... resourceNames) {
        Properties properties = new Properties();
        try {
            for (int ix = 0; ix < resourceNames.length; ix++) {
                properties.load(ClassLoader.getSystemClassLoader().getResourceAsStream(resourceNames[ix]));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return properties;
    }
}
