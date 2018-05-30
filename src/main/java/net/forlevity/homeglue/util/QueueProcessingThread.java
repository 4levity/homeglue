/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.util;

import com.google.common.annotations.VisibleForTesting;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;

import java.time.Instant;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

/**
 * A thread that processes items one at a time. Internally, the items are
 * placed in a queue. Each item is passed to the processor as it dequeues.
 * Call interrupt() to stop processing and quit.
 *
 * @param <T> item type
 */
@Log4j2
@Accessors(chain = true)
public class QueueProcessingThread<T> extends Thread implements Consumer<T> {

    private static final int DEFAULT_QUEUE_SIZE_ALERT_THRESHOLD = 50;
    private static final int DEFAULT_MIN_SECS_BETWEEN_QUEUE_SIZE_ALERTS = 5;

    @Getter
    private volatile boolean running = false;

    @Getter
    @Setter
    private int queueSizeAlertThreshold = DEFAULT_QUEUE_SIZE_ALERT_THRESHOLD;

    @Getter
    @Setter
    private long minSecondsBetweenQueueLengthAlerts = DEFAULT_MIN_SECS_BETWEEN_QUEUE_SIZE_ALERTS;

    @VisibleForTesting
    @Getter
    private final BlockingQueue<T> queue;
    private final Consumer<T> processor;
    private final Class<T> itemType;
    private Instant suppressQueueLengthAlertUntil = Instant.now();

    /**
     * Create a queue processing thread. Items are passed to the processor
     * one at a time. Note that the caller-supplied processor must call
     * Thread.currentThread().interrupt() before returning if it has been
     * interrupted.
     *
     * @param itemType item type - must be an interface
     * @param processor processor to call on items
     */
    @SuppressWarnings("unchecked")
    public QueueProcessingThread(Class<T> itemType, Consumer<T> processor) {
        this.itemType = itemType;
        this.queue = new LinkedBlockingQueue<>();
        this.processor = processor;
    }

    @Override
    public void run() {
        running = true;
        while (running) {
            try {
                processQueue();
            } catch (InterruptedException e) {
                interrupt();
            }
            if (isInterrupted()) {
                log.debug("interrupted, exiting {} queue processor", itemType.getSimpleName());
                running = false;
            }
        }
    }

    @VisibleForTesting
    public void processQueue() throws InterruptedException {
        while (processSingleQueueEntry()) {
            int size = queue.size();
            Instant now = Instant.now();
            if (size > getQueueSizeAlertThreshold() && suppressQueueLengthAlertUntil.isBefore(now)) {
                log.warn("queue length alert for {}! {} > {}",
                        itemType.getSimpleName(), size, DEFAULT_QUEUE_SIZE_ALERT_THRESHOLD);
                suppressQueueLengthAlertUntil = now.plusSeconds(minSecondsBetweenQueueLengthAlerts);
            }
        }
    }

    private boolean processSingleQueueEntry() throws InterruptedException {
        T entry = queue.take();
        try {
            processor.accept(entry);
        } catch (RuntimeException e) {
            log.error("unexpected exception in {} queue processor (continuing)", itemType.getSimpleName(), e);
        }
        return !queue.isEmpty() && !isInterrupted();
    }

    @Override
    public void accept(T item) {
        queue.offer(item);
    }
}
