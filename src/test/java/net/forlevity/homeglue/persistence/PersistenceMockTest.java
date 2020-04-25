/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.persistence;

import net.forlevity.homeglue.testing.HomeglueTests;
import net.forlevity.homeglue.testing.ThisTestException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class PersistenceMockTest extends HomeglueTests {

    @Mock
    Session mockSession;

    @Mock
    Transaction mockTransaction;

    H2HibernateService persistence;

    @BeforeEach
    void init() throws SQLException {
        initMocks(this);
        when(mockSession.getTransaction()).thenReturn(mockTransaction);
        when(mockTransaction.isActive()).thenReturn(true);
        persistence = new H2HibernateService("mock.persistence.properties", (settings) -> mockSessionFactory());
        persistence.start();
    }

    @Test
    void rollsBack() {
        assertThrows(ThisTestException.class, () -> persistence.exec(HomeglueTests::throwThisTestException));
        verify(mockTransaction, never()).commit();
        verify(mockTransaction, times(1)).rollback();
        verify(mockSession, times(1)).close();
    }

    @Test
    void commits() {
        assertEquals("correct", persistence.exec(session -> "correct"));
        verify(mockTransaction, never()).rollback();
        verify(mockTransaction, times(1)).commit();
        verify(mockSession, times(1)).close();
    }

    private SessionFactory mockSessionFactory() {
        SessionFactory sf = mock(SessionFactory.class);
        when(sf.openSession()).thenReturn(mockSession);
        return sf;
    }
}
