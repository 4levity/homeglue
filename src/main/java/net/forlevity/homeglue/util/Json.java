/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;

/**
 * Static holder for global reusable Jackson ObjectMapper and convenience methods to generate/parse JSON.
 */
@Log4j2
public class Json {

    public static final ObjectMapper JACKSON = new ObjectMapper().findAndRegisterModules();

    private Json() {}

    /**
     * Convert an object to JSON string.
     *
     * @param object convertible object
     * @return string
     */
    public static String toJson(Object object) {
        Preconditions.checkNotNull(object);
        try {
            return JACKSON.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("failed to serialize object of type {}", object.getClass().getSimpleName());
            throw new RuntimeException(e);
        }
    }

    public static <T> T fromJson(String text, Class<T> clazz) {
        Preconditions.checkNotNull(clazz);
        Preconditions.checkNotNull(text);
        try {
            return JACKSON.readValue(text, clazz);
        } catch (IOException e) {
            log.error("failed to deserialize object of type {}", clazz.getCanonicalName());
            throw new RuntimeException(e);
        }
    }
}
