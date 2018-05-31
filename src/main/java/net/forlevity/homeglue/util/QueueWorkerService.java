/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.util;

import com.google.common.annotations.VisibleForTesting;
import lombok.extern.log4j.Log4j2;

import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;

@Log4j2
public class QueueWorkerService<T> extends RunnableExecutionThreadService implements Consumer<T> {

    private final QueueWorker<T> worker;

    protected QueueWorkerService(Class<T> itemType) {
        worker = new QueueWorker<T>(itemType, this::handle);
    }

    @Override
    protected void runUntilInterrupted() {
        worker.run();
    }

    @Override
    public void accept(T item) {
        worker.accept(item);
    }

    @VisibleForTesting
    public void processQueue() throws InterruptedException {
        worker.processQueue();
    }

    @VisibleForTesting
    public BlockingQueue<T> getQueue() {
        return worker.getQueue();
    }

    /**
     * Subclass implements this to process an item. Default implementation logs.
     *
     * @param item item
     */
    protected void handle(T item) {
        log.info("item: {}", item);
    }
}
