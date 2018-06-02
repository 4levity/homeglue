/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.ifttt;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import net.forlevity.homeglue.http.SimpleHttpClient;
import net.forlevity.homeglue.util.Json;
import org.apache.http.entity.ContentType;

import java.io.IOException;

/**
 * Interface to trigger IFTTT Maker Webhooks. See https://ifttt.com/maker_webhooks .
 */
@Log4j2
public class IftttMakerWebhookClient {

    private boolean notifiedDisabled = false;

    private final SimpleHttpClient httpClient;
    private final Json json;
    private final String key;

    @Inject
    public IftttMakerWebhookClient(SimpleHttpClient httpClient, Json json, @Named("ifttt.webhooks.key") String key) {
        this.httpClient = httpClient;
        this.json = json;
        this.key = key;
    }

    /**
     * Trigger an event, specifying values for the parameters.
     *
     * @param event event
     * @param value1 value or null
     * @param value2 value or null
     * @param value3 value or null
     */
    public synchronized void trigger(String event, String value1, String value2, String value3) {
        if (Strings.isNullOrEmpty(key)) {
            if (!notifiedDisabled) {
                log.info("IFTTT disabled, to enable put \"ifttt.webhooks.key=xxxxx\" in configuration file");
                notifiedDisabled = true; // only log this message once
            }
        } else {
            String url = String.format("https://maker.ifttt.com/trigger/%s/with/key/%s", event, key);
            String payload;
            if (value1 == null && value2 == null && value3 == null) {
                payload = "";
            } else {
                payload = json.toJson(new WebhookPostBody(value1, value2, value3));
            }
            try {
                String result = httpClient.post(url, null, payload, ContentType.APPLICATION_JSON);
                log.debug("result: {}", result);
            } catch (IOException e) {
                log.warn("failed to trigger IFTTT {} event because {} {}",
                        event, e.getClass().getSimpleName(), e.getMessage());
            }
        }
    }

    /**
     * Trigger an event without parameters.
     *
     * @param event event
     */
    public void trigger(String event) {
        trigger(event, null, null, null);
    }

    /**
     * Trigger an event with an IftttMakerWebHookClient.Event object.
     *
     * @param event event
     */
    public void trigger(Event event) {
        trigger(event.event, event.value1, event.value2, event.value3);
    }

    @AllArgsConstructor
    @ToString
    public static class Event {
        String event;
        String value1;
        String value2;
        String value3;
    }
}
