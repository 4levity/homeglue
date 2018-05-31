/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.ifttt;

import com.google.inject.ImplementedBy;
import lombok.AllArgsConstructor;
import lombok.ToString;

/**
 * Interface to trigget IFTTT Maker Webhooks. See https://ifttt.com/maker_webhooks .
 */
@ImplementedBy(IftttMakerWebhookClientImpl.class)
public interface IftttMakerWebhookClient {

    /**
     * Trigger an event, specifying values for the parameters.
     *
     * @param event event
     * @param value1 value or null
     * @param value2 value or null
     * @param value3 value or null
     */
    void trigger(String event, String value1, String value2, String value3);

    /**
     * Trigger an event without parameters.
     *
     * @param event event
     */
    void trigger(String event);

    /**
     * Trigger an event with an IftttMakerWebHookClient.Event object.
     *
     * @param event event
     */
    void trigger(Event event);

    @AllArgsConstructor
    @ToString
    class Event {
        String event;
        String value1;
        String value2;
        String value3;
    }
}
