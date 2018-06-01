/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.testing;

import java.util.HashSet;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * PARTIAL IMPLEMENTATION of a blocking queue that ensures uniqueness of items in the queue.
 *
 * @param <T> item type
 */
public class LinkedUniqueQueue<T> extends LinkedBlockingQueue<T> {

    private final HashSet<T> items = new HashSet<>();

    @Override
    public boolean offer(T t) {
        synchronized (items) {
            return !items.add(t) || super.offer(t);
        }
    }

    @Override
    public T take() throws InterruptedException {
        T item = super.take();
        synchronized (items) {
            items.remove(item);
        }
        return item;
    }

    // TODO: delegate instead of extending, support all BlockingQueue interface
}
