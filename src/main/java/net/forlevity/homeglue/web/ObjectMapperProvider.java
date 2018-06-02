/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

/**
 * Provides Jackson ObjectMapper to RESTeasy.
 */
@Provider
@Singleton
public class ObjectMapperProvider implements ContextResolver<ObjectMapper> {

    private final ObjectMapper jackson;

    @Inject
    public ObjectMapperProvider(ObjectMapper jackson) {
        this.jackson = jackson;
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return jackson;
    }
}
