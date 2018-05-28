/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.ifttt;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.extern.log4j.Log4j2;
import net.forlevity.homeglue.http.SimpleHttpClient;
import net.forlevity.homeglue.util.Json;
import org.apache.http.entity.ContentType;

import java.io.IOException;

/**
 * Implementation of IftttMakerWebhookClient.
 */
@Log4j2
@Singleton
public class IftttMakerWebhookClientImpl implements IftttMakerWebhookClient {

    private boolean notifiedDisabled = false;

    private final String key;
    private final SimpleHttpClient httpClient;

    @Inject
    public IftttMakerWebhookClientImpl(SimpleHttpClient httpClient, @Named("ifttt.webhooks.key") String key) {
        this.httpClient = httpClient;
        this.key = key;
    }

    @Override
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
                payload = null;
            } else {
                payload = Json.toJson(new WebhookPostBody(value1, value2, value3));
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

    @Override
    public void trigger(String event) {
        trigger(event, null, null, null);
    }
}
