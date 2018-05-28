/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.persistence;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.AbstractIdleService;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.github.lukehutch.fastclasspathscanner.scanner.ClassInfo;
import io.github.lukehutch.fastclasspathscanner.scanner.ScanResult;
import lombok.extern.log4j.Log4j2;
import net.forlevity.homeglue.util.ResourceHelper;
import org.h2.tools.Server;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import javax.persistence.Entity;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * PersistenceService implementation.
 */
@Log4j2
public class PersistenceServiceImpl extends AbstractIdleService implements PersistenceService {

    private final Integer h2WebserverPort;
    private final Properties settings;

    private SessionFactory sessionFactory = null;
    private Server h2WebServer = null;

    @Inject
    PersistenceServiceImpl(@Named("persistence.settings.resource") String persistenceSettingsResource) {
        settings = ResourceHelper.resourceAsProperties(persistenceSettingsResource);
        boolean h2WebserverEnable = Boolean.valueOf(settings.getProperty("h2.webserver.enable"));
        if (h2WebserverEnable) {
            h2WebserverPort = Integer.valueOf(settings.getProperty("h2.webserver.port"));
        } else {
            h2WebserverPort = null;
        }
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
    void start() {
        if (isRunning()) {
            throw new IllegalStateException("service is already running");
        }
        if (h2WebserverPort != null) {
            try {
                h2WebServer = Server.createWebServer("-webPort", h2WebserverPort.toString());
                h2WebServer.start();
                log.info("H2 db web interface at http://localhost:{}/ (local access only)", h2WebserverPort);
            } catch (SQLException e) {
                log.warn("failed to start H2 db webserver at http://localhost:{}/", h2WebserverPort, e);
            }
        }
        StandardServiceRegistry registry = new StandardServiceRegistryBuilder().applySettings(settings).build();
        MetadataSources metadata = new MetadataSources(registry);

        // search for and map Entity classes
        ScanResult scanResult = new FastClasspathScanner(settings.getProperty("entity.search.package")).scan();
        List<String> entityClasses = scanResult.getNamesOfClassesWithAnnotation(Entity.class);
        entityClasses.forEach(metadata::addAnnotatedClassName);

        // create session factory
        sessionFactory = metadata.buildMetadata().buildSessionFactory();

        // startup log
        Map<String, ClassInfo> classInfoMap = scanResult.getClassNameToClassInfo();
        String classNames = entityClasses.stream().map(name -> classInfoMap.get(name).getClassRef().getSimpleName())
                .collect(Collectors.joining(", "));
        log.info("Persistence mapped classes [{}] using {}", classNames, getJdbcUrl());
    }

    @VisibleForTesting
    void stop() {
        if (h2WebServer != null && h2WebServer.isRunning(false)) {
            h2WebServer.stop();
            h2WebServer = null;
        }
        sessionFactory = null;
    }

    @Override
    public <RT> RT exec(net.forlevity.homeglue.persistence.Transaction<RT> operation) {
        if (sessionFactory == null) {
            throw new IllegalStateException("sessionFactory not initialized");
        }
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        RT result;
        boolean completed = false;
        try {
            result = operation.apply(session);
            completed = true;
        } finally {
            finish(completed, session);
        }
        return result;
    }

    private void finish(boolean completed, Session session) {
        boolean rollback = !completed;
        org.hibernate.Transaction transaction = null;
        try {
            transaction = session.getTransaction();
            if (transaction == null || !transaction.isActive()) {
                throw new IllegalStateException("finish() called with no active Transaction");
            }
            if (rollback) {
                log.warn("errors occurred during transaction, rolling back");
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

    private String getJdbcUrl() {
        return exec(session -> {
            String[] url = new String[1];
            session.doWork(connection -> url[0] = connection.getMetaData().getURL());
            return url[0];
        });
    }
}
