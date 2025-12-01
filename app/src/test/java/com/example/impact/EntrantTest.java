package com.example.impact;

import com.example.impact.model.Entrant;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Tests for the {@link Entrant} domain model.
 */
public class EntrantTest {

    /**
     * Confirms entrants report their role as "entrant".
     */
    @Test
    public void testEntrantRoleIsCorrect() {
        Entrant entrant = new Entrant("E1", "John", "john@mail.com", "1234567890");
        assertEquals("entrant", entrant.getRole());
    }

    /**
     * Ensures getters expose constructor-initialized data.
     */
    @Test
    public void testEntrantFields() {
        Entrant entrant = new Entrant("E2", "Alice", "alice@mail.com", null);

        assertEquals("E2", entrant.getId());
        assertEquals("Alice", entrant.getName());
        assertEquals("alice@mail.com", entrant.getEmail());
        assertNull(entrant.getPhone());
    }
}
