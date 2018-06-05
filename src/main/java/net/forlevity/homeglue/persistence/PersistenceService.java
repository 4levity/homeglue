/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.persistence;

import com.google.common.util.concurrent.Service;
import com.google.inject.ImplementedBy;
import org.hibernate.Session;

import java.util.function.Function;

/**
 * PersistenceService service to execute work units in a managed Hibernate session.
 */
@ImplementedBy(H2HibernateService.class)
public interface PersistenceService extends Service {

    /**
     * Execute a unit of work on a Hibernate session with an open transaction.
     * The transaction will be committed on completion. However, if the
     * operation threw a runtime exception or if the commit failed, the
     * transaction will be rolled back instead.
     *
     * @param operation work unit
     * @param <RT> return type of work unit
     * @return result of work
     */
    <RT> RT exec(Function<Session, RT> operation);

    /**
     * Try to load an object that might be a synthetic proxy object so that its
     * fields may be accessed outside of an active session.
     *
     * @param entityClass entity class
     * @param entity entity object (maybe a proxy)
     * @param <T> entity type
     * @return entity (not a proxy)
     */
    <T> T unproxy(Class<T> entityClass, T entity);
}
