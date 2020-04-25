/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.persistence;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.AbstractIdleService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.extern.log4j.Log4j2;
import net.forlevity.homeglue.util.ResourceHelper;
import org.flywaydb.core.Flyway;
import org.h2.tools.Server;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

/**
 * PersistenceService implementation using H2 embedded.
 */
@Log4j2
@Singleton
public class H2HibernateService extends AbstractIdleService implements PersistenceService {

    private final Integer h2WebserverPort;
    private final Properties settings;
    private final ReadWriteLock safeShutdown = new ReentrantReadWriteLock();
    private final SessionFactoryFactory sessionFactoryFactory;

    private SessionFactory sessionFactory = null;
    private Server h2WebServer = null;
    private boolean started = false;
    private boolean stopped = false;
    private Connection keepalive;

    @Inject
    H2HibernateService(@Named("persistence.settings.resource") String persistenceSettingsResource,
                       SessionFactoryFactory sessionFactoryFactory) {
        settings = ResourceHelper.resourceAsProperties(persistenceSettingsResource);
        boolean h2WebserverEnable = Boolean.valueOf(settings.getProperty("h2.webserver.enable"));
        if (h2WebserverEnable) {
            h2WebserverPort = Integer.valueOf(settings.getProperty("h2.webserver.port"));
        } else {
            h2WebserverPort = null;
        }
        this.sessionFactoryFactory = sessionFactoryFactory;
    }

    @Override
    protected void startUp() throws Exception {
        start();
    }

    @Override
    protected void shutDown() throws Exception {
        stop();
    }

    @VisibleForTesting
    void start() throws SQLException {

        if (started) {
            throw new IllegalStateException("service was already started");
        }
        started = true;

        startKeepalive();

        if (Boolean.valueOf(settings.getProperty("flyway.auto"))) {
            updateSchema(); // perform database schema updates if necessary
        }

        sessionFactory = sessionFactoryFactory.create(settings);

        if (h2WebserverPort != null) {
            h2WebServer = Server.createWebServer("-webPort", h2WebserverPort.toString());
            h2WebServer.start();
            log.info("H2 db web interface at http://localhost:{}/ (local access only)", h2WebserverPort);
        }

        log.info("connected to {}", this::getJdbcUrl);

        stopKeepalive();
    }

    private void startKeepalive() throws SQLException {
        keepalive = null; // temporary connection keeps db from resetting between Flyway and Hibernate init
        if (getConnectionUrl().startsWith("jdbc:h2:mem")) {
            keepalive = getConnection();
        }
    }

    private void stopKeepalive() {
        if (keepalive != null) {
            try {
                keepalive.close();
            } catch (SQLException e) {
                log.error("error closing keepalive connection");
            }
        }
    }

    @VisibleForTesting
    void stop() throws SQLException {
        if (!stopped) {
            stopped = true;
            if (h2WebServer != null && h2WebServer.isRunning(false)) {
                h2WebServer.stop();
                h2WebServer = null;
            }
            safeShutdown.writeLock().lock(); // wait for in-flight transactions (never unlocks)
            if (sessionFactory == null) {
                log.error("stop() called but no sessionFactory exists");
            } else {
                sessionFactory.close();
                sessionFactory = null;
                h2Shutdown();
            }
        }
    }

    @Override
    public <RT> RT exec(Function<Session, RT> operation) {
        if (sessionFactory == null) {
            throw new IllegalStateException("sessionFactory not initialized");
        }
        Lock lock = safeShutdown.readLock();
        lock.lock();
        try {
            return unlockedExec(operation);
        } finally {
            lock.unlock();
        }
    }

    @Override
    @SuppressWarnings("unchecked") // is checked!
    public <T> T unproxy(Class<T> entityClass, T entity) {
        if (entity == null) {
            return null;
        }
        Object unproxied = Hibernate.unproxy(entity);
        if (!entityClass.isAssignableFrom(unproxied.getClass())) {
            throw new IllegalArgumentException("unexpected type on unproxied entity");
        }
        return (T) unproxied;
    }

    private <RT> RT unlockedExec(Function<Session, RT> operation) {
        RT result;
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        Throwable thrown = null;
        boolean completed = false;
        try {
            result = operation.apply(session);
        } catch (Throwable e) {
            thrown = e;
            throw e;
        } finally {
            finish(thrown, session);
        }
        return result;
    }

    private void finish(Throwable thrown, Session session) {
        boolean rollback = thrown != null;
        Transaction transaction = null;
        try {
            transaction = session.getTransaction();
            if (transaction == null || !transaction.isActive()) {
                throw new IllegalStateException("finish() called with no active Transaction");
            }
            if (rollback) {
                log.info("thrown during transaction: {} {}",
                        thrown.getClass().getSimpleName(), thrown.getMessage());
                // rollback will occur in finally block
            } else {
                try {
                    transaction.commit();
                } catch (RuntimeException e) {
                    log.error("commit failed, switching to rollback", e);
                    rollback = true;
                    throw e;
                    // rollback will occur in finally block
                }
            }
        } finally {
            if (rollback && transaction != null && transaction.isActive()) {
                try {
                    transaction.rollback();
                } catch (RuntimeException e) {
                    // just log rollback failure. the original failure is being thrown through this finally block.
                    log.error("rollback failed", e);
                }
            }
            session.close();
        }
    }

    private String getConnectionUrl() {
        return settings.getProperty("hibernate.connection.url");
    }

    private String getJdbcUrl() {
        String jdbcUrl = getConnectionUrl();
        if (jdbcUrl.startsWith("jdbc:")) {
            // if real db, get actual connection URL used by Hibernate
            // may be different than configured connection URL, e.g. connection params not included
            jdbcUrl = exec(session -> {
                String[] url = new String[1];
                session.doWork(connection -> url[0] = connection.getMetaData().getURL());
                return url[0];
            });
        }
        return jdbcUrl;
    }

    private String getUsername() {
        return settings.getProperty("hibernate.connection.username");
    }

    private String getPassword() {
        return settings.getProperty("hibernate.connection.password");
    }

    private void h2Shutdown() throws SQLException {
        String dbUrl = getConnectionUrl();
        if (!dbUrl.toUpperCase().contains("DB_CLOSE_ON_EXIT=FALSE")) {
            return; // we only need to do something if using H2 embedded with manual shutdown needed
        }
        Connection connection = null;
        Statement statement = null;
        try {
            connection = getConnection();
            statement = connection.createStatement();
            statement.executeUpdate("SHUTDOWN");
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                log.error("failed to close connection");
            }
        }
        log.debug("H2 shutdown successful");
    }

    private Connection getConnection() throws SQLException {
        String username = getUsername();
        String password = getPassword();
        Connection connection;
        connection = DriverManager.getConnection(getConnectionUrl(), username, password);
        return connection;
    }

    private void updateSchema() {
        Flyway flyway = Flyway.configure()
                .locations("classpath:db/migrations")
                .dataSource(getConnectionUrl(), getUsername(), getPassword())
                .load();
        flyway.migrate();
    }
}
