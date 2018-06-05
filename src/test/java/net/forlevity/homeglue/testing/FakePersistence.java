/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.testing;

import com.google.common.util.concurrent.AbstractIdleService;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.forlevity.homeglue.persistence.PersistenceService;
import org.hibernate.Session;
import org.hibernate.SimpleNaturalIdLoadAccess;

import java.io.Serializable;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Fake persistence service - executes work units but uses a fake session to do it. Supports resolving id or naturalId
 * to arbitrary objects. Not all Session methods supported, some just always return null.
 */
@Accessors(chain = true)
public class FakePersistence extends AbstractIdleService implements PersistenceService {

    @Getter
    private final Session session;

    @Setter
    private Function<Serializable, Object> resolver = clazz -> null;

    @SuppressWarnings("unchecked")
    public FakePersistence() {
        session = mock(Session.class);
        when(session.load((Class<?>)any(), any()))
                .thenAnswer(invocation -> resolver.apply(invocation.getArgument(1)));
        SimpleNaturalIdLoadAccess simpleNaturalIdLoadAccess = mock(SimpleNaturalIdLoadAccess.class);
        when(simpleNaturalIdLoadAccess.load(any()))
                .thenAnswer(invocation -> resolver.apply(invocation.getArgument(0)));
        when(session.bySimpleNaturalId((Class<Object>) any())).thenReturn(simpleNaturalIdLoadAccess);
        // TODO: mock other methods as needed
    }

    @Override
    public <RT> RT exec(Function<Session, RT> operation) {
        return operation.apply(session);
    }

    @Override
    public <T> T unproxy(Class<T> entityClass, T entity) {
        return entity;
    }

    @Override
    protected void startUp() throws Exception {
    }

    @Override
    protected void shutDown() throws Exception {
    }
}
