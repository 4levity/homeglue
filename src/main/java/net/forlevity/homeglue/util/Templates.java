/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.util;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.map.CompositeMap;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Wrapper for Pebble Template Engine.
 */
@Log4j2
public class Templates {

    private final Map<String, Object> defaultContext;
    private final Map<String, PebbleTemplate> templatesByFilename = new ConcurrentHashMap<>();
    private final PebbleEngine engine = new PebbleEngine.Builder().build();

    /**
     * Inject DefaultContext to create Templates.
     *
     * @param defaultContext defaults
     */
    @Inject
    public Templates(DefaultContext defaultContext) {
        this.defaultContext = defaultContext.immutableMap;
    }

    /**
     * Create a Templates provider with a default context map.
     *
     * @param defaultContext defaults
     */
    public Templates(Map<String, Object> defaultContext) {
        this.defaultContext = ImmutableMap.copyOf(defaultContext);
    }

    /**
     * Render a template into a String.
     *
     * @param templateFile filename
     * @param callerContext additional context
     * @return string
     */
    public String render(String templateFile, Map<String, Object> callerContext) {
        Map<String, Object> totalContext = new CompositeMap<>(defaultContext, callerContext);
        StringWriter writer = new StringWriter();
        try {
            PebbleTemplate template = getTemplate(templateFile);
            template.evaluate(writer, totalContext);
        } catch (NullPointerException | IOException | PebbleException e) {
            log.error("Unexpected exception evaluating template {}", templateFile, e);
        }
        return writer.toString();
    }

    private PebbleTemplate getTemplate(String templateFile) throws PebbleException {
        PebbleTemplate template = templatesByFilename.get(templateFile);
        if (template == null) {
            template = engine.getTemplate(templateFile);
            templatesByFilename.put(templateFile, template);
        }
        return template;
    }

    /**
     * Use to inject default context if desired.
     */
    public class DefaultContext {

        private final ImmutableMap<String, Object> immutableMap;

        @Inject
        public DefaultContext() {
            immutableMap = ImmutableMap.of();
        }

        /**
         * Create a context
         * @param values
         */
        public DefaultContext(Map<String, ?> values) {
            immutableMap = ImmutableMap.copyOf(values);
        }
    }
}
