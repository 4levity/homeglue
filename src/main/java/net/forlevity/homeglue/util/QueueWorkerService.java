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

@Log4j2
public class QueueWorkerService<T> extends AbstractExecutionThreadService implements Consumer<T> {

    private final QueueWorker<T> worker;
    private Thread executionThread = null;

    protected QueueWorkerService(Class<T> itemType) {
        worker = new QueueWorker<T>(itemType, this::handle);
    }

    /**
     * Run until interrupted.
     */
    @Override
    protected final void run() {
        executionThread = Thread.currentThread();
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

    /**
     * Subclass implements this to process an item. Default implementation logs.
     *
     * @param item item
     */
    protected void handle(T item) {
        log.info("item: {}", item);
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
