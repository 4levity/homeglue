/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.util;

import com.google.common.util.concurrent.AbstractExecutionThreadService;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

/**
 * A Guava AbstractExecutionThreadService that triggers shutdown by interrupting the main thread.
 */
@Log4j2
public class RunnableExecutionThreadService extends AbstractExecutionThreadService {

    @Setter(AccessLevel.PROTECTED)
    private Runnable runnable;

    private Thread executionThread = null;

    /**
     * Create a new service that will execute the given runnable. Subclass may
     * optionally pass in null and override runUntilInterrupted() instead.
     *
     * @param runUntilInterrupted runnable that exits if interrupted
     */
    public RunnableExecutionThreadService(Runnable runUntilInterrupted) {
        this.runnable = runUntilInterrupted;
    }

    /**
     * Create a new service without a runnable. With this constructor, either
     * override runUntilInterrupted() or call setRunnable() before starting.
     */
    public RunnableExecutionThreadService() {
        this.runnable = null;
    }

    /**
     * Subclass may override for startup. Does nothing by default.
     *
     * @throws Exception on error
     */
    @Override
    protected void startUp() throws Exception {
    }

    @Override
    protected final void run() {
        executionThread = Thread.currentThread();
        runUntilInterrupted();
    }

    /**
     * Subclass may override this method rather than providing a runnable.
     */
    protected void runUntilInterrupted() {
        if (runnable == null) {
            log.warn("No worker for {} service, idling until shutdown", this.getClass().getSimpleName());
            try {
                Thread.currentThread().join();
            } catch (InterruptedException e) {
                // OK
            }
        } else {
            this.runnable.run();
        }
    }

    /**
     * Subclass may override for shutdown. Does nothing by default.
     *
     * @throws Exception on error
     */
    @Override
    protected void shutDown() throws Exception {
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
}
