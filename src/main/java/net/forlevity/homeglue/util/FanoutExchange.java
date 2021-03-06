/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.util;

import com.google.inject.Inject;

import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Defines a simple typed exchange where messages of some type are distributed to a list of consumers.
 * Distribution is on handler thread, consumers are assumed to be fast.
 *
 * @param <T> message type
 */
public class FanoutExchange<T> implements Consumer<T> {

    private final Collection<Consumer<T>> sinks;

    @Inject
    public FanoutExchange(Set<Consumer<T>> sinks) {
        this.sinks = sinks;
    }

    @Override
    public void accept(T item) {
        sinks.forEach(sink -> sink.accept(item));
    }
}
