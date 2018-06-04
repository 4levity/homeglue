/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.device;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.time.Instant;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Log4j2
public class PollerCommander {

    private static final int WARN_EARLY_MILLIS = 500;

    @Getter
    private final String name;

    private final int periodMillis;
    private final int minIdleBetweenMillis;
    private final Runnable poller;
    private ScheduledThreadPoolExecutor executor;
    private long idleStartTime;

    public PollerCommander(String name, Runnable poller, int periodMillis, int minIdleBetweenMillis) {
        this.name = name;
        this.periodMillis = periodMillis;
        this.poller = poller;
        this.minIdleBetweenMillis = minIdleBetweenMillis;
    }

    public boolean isStarted() {
        return executor != null;
    }

    public synchronized void start() {
        if (isStarted()) {
            throw new IllegalStateException("already started");
        }
        executor = new ScheduledThreadPoolExecutor(1);
        idleStartTime = 0L;
        executor.scheduleAtFixedRate(this::tryPoll, 0L, periodMillis, TimeUnit.MILLISECONDS);
    }

    public synchronized void stop() {
        if (!isStarted()) {
            throw new IllegalStateException("never started");
        }
        if (executor.isShutdown()) {
            throw new IllegalStateException("already shutdown");
        }
        executor.shutdown();
    }

    public Future<Command.Result> runCommand(Callable<Command.Result> command) {
        return executor.submit(() -> {
            long callableStartTime = Instant.now().toEpochMilli();
            long idleMillis = callableStartTime - idleStartTime;
            if (idleMillis < minIdleBetweenMillis) {
                Thread.sleep(minIdleBetweenMillis - idleMillis);
            }
            try {
                return command.call();
            } finally {
                idleStartTime = Instant.now().toEpochMilli();
            }
        });
    }

    private void tryPoll() {
        long pollStartTime = Instant.now().toEpochMilli();
        long idleMillis = pollStartTime - idleStartTime;
        if (idleMillis < minIdleBetweenMillis) {
            log.warn("Poller {} was only idle for {} ms, skipping trigger", getName(), idleMillis);
        } else {
            try {
                poller.run(); // do poll operation
            } finally {
                idleStartTime = Instant.now().toEpochMilli();
            }
        }
    }
}
