/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.device;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.time.Instant;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Polls a device periodically, enforcing limits
 */
@Log4j2
public class PollerCommander {

    private static final int WARN_EARLY_MILLIS = 500;
    private static final int MISSED_TRIGGERS_PER_WARNING = 10;

    @Getter
    private final String name;

    private final int periodMillis;
    private final int minIdleBetweenMillis;
    private final Runnable poller;
    private final ScheduledExecutorService executor;
    private long idleStartTime;
    private boolean commandJustIssued = false;
    private ScheduledFuture<?> pollerFuture = null;
    private int ignoredMissedTriggers = 0;
    private Lock pollingLock = new ReentrantLock();

    public PollerCommander(ScheduledExecutorService executor,
                           String name, Runnable poller, int periodMillis, int minIdleBetweenMillis) {
        this.name = name;
        this.periodMillis = periodMillis;
        this.poller = poller;
        this.minIdleBetweenMillis = minIdleBetweenMillis;
        this.executor = executor;
    }

    public synchronized void start() {
        if (isStarted()) {
            throw new IllegalStateException("already started");
        }
        idleStartTime = 0L;
        pollerFuture = executor.scheduleAtFixedRate(this::tryPoll, 0L, periodMillis, TimeUnit.MILLISECONDS);
    }

    public synchronized void stop() {
        if (!isStarted()) {
            throw new IllegalStateException("never started");
        }
        pollerFuture.cancel(true);
    }

    public boolean isStarted() {
        return pollerFuture != null;
    }

    /**
     * Run a command on a device that we are also polling. Don't do it too close to the same time as the poll.
     *
     * @param command command
     * @return future result (may take a few seconds)
     */
    public Future<Command.Result> runCommand(Callable<Command.Result> command) {
        return executor.submit(() -> {
            pollingLock.lock();
            try {
                long callableStartTime = Instant.now().toEpochMilli();
                long idleMillis = callableStartTime - idleStartTime;
                if (idleMillis < minIdleBetweenMillis) {
                    Thread.sleep(minIdleBetweenMillis - idleMillis);
                }
                try {
                    return command.call();
                } finally {
                    idleStartTime = Instant.now().toEpochMilli();
                    commandJustIssued = true;
                }
            } finally {
                pollingLock.unlock();
            }
        });
    }

    private void tryPoll() {
        pollingLock.lock();
        try {
            long pollStartTime = Instant.now().toEpochMilli();
            long idleMillis = pollStartTime - idleStartTime;
            if (idleMillis < minIdleBetweenMillis) {
                if (!commandJustIssued) { // ignore skipped trigger if the last thing we did was issue a command
                    if (ignoredMissedTriggers < MISSED_TRIGGERS_PER_WARNING) {
                        ignoredMissedTriggers++;
                    } else {
                        log.warn("Poller {} missed {} triggers, last idle for {} ms",
                                getName(), ignoredMissedTriggers, idleMillis);
                        ignoredMissedTriggers = 0;
                    }
                }
            } else {
                try {
                    poller.run(); // do poll operation
                } finally {
                    idleStartTime = Instant.now().toEpochMilli();
                }
            }
        } finally {
            commandJustIssued = false;
            pollingLock.unlock();
        }
    }
}
