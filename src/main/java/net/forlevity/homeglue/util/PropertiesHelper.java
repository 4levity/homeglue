/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Static convenience methods related to Properties.
 */
public class PropertiesHelper {

    private PropertiesHelper() {}

    /**
     * Try to read properties from a file, and merge with existing properties, overwriting existing values.
     * If the file is not found, do nothing. If the file exists but cannot be read, throw RuntimeException!
     *
     * @param properties initial properties
     * @param filename file to merge
     */
    public static void tryMergeFile(Properties properties, String filename) {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(filename);
        } catch (FileNotFoundException e) {
            // ignore
        }
        if (inputStream != null) {
            try {
                properties.load(inputStream);
            } catch (IOException e) {
                System.err.println("failed to load file " + filename);
                throw new RuntimeException(e);
            }
        }
    }
}
