/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.util;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import lombok.extern.log4j.Log4j2;

import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;

/**
 * Guava Service wrapper for QueueWorker. If items are consumed before service is started,
 * they will not be processed until it does.
 *
 * @param <T> type of object in queue
 */
@Log4j2
public class QueueWorkerService<T> extends AbstractExecutionThreadService implements Consumer<T> {

    private final QueueWorker<T> worker;
    private final ServiceDependencies dependencies;
    private Thread executionThread = null;

    /**
     * Create a new QueueWorkerService. Dequeued items are handled by the given consumer.
     *
     * @param itemType type
     * @param serialConsumer consumer
     * @param dependencies service dependencies, or null
     */
    public QueueWorkerService(Class<T> itemType, Consumer<T> serialConsumer, ServiceDependencies dependencies) {
        this.worker = new QueueWorker<>(itemType, serialConsumer);
        this.dependencies = dependencies;
    }

    /**
     * Create a new QueueWorkerService. Subclass also overrides handle() to process dequeued items.
     *
     * @param itemType type
     * @param dependencies service dependencies, or null
     */
    protected QueueWorkerService(Class<T> itemType, ServiceDependencies dependencies) {
        this.worker = new QueueWorker<>(itemType, this::handle);
        this.dependencies = dependencies;
    }

    /**
     * Subclass overrides this to process an item if using the protected constructor.
     *
     * @param item item
     */
    protected void handle(T item) {
        log.warn("{} default handler for {}", getClass().getSimpleName(), item);
    }

    /**
     * Run until interrupted.
     */
    @Override
    protected final void run() {
        executionThread = Thread.currentThread();
        if (dependencies != null) {
            dependencies.waitForDependencies(this);
        }
        worker.run();
    }

    /**
     * Consume an item by putting it on the queue.
     *
     * @param item item
     */
    @Override
    public void accept(T item) {
        worker.accept(item);
    }

    /**
     * Interrupt the execution thread when shutdown is requested.
     */
    @Override
    protected void triggerShutdown() {
        if (executionThread != null) {
            executionThread.interrupt();
        } else {
            log.warn("shutdown triggered when execution thread was not yet running");
        }
    }

    @VisibleForTesting
    public void processQueue() throws InterruptedException {
        worker.processQueue();
    }

    @VisibleForTesting
    public BlockingQueue<T> getQueue() {
        return worker.getQueue();
    }
}
