package com.example.impact;

import com.example.impact.model.Entrant;
import org.junit.Test;
import static org.junit.Assert.*;

public class EntrantTest {

    @Test
    public void testEntrantRoleIsCorrect() {
        Entrant entrant = new Entrant("E1", "John", "john@mail.com", "1234567890");
        assertEquals("entrant", entrant.getRole());
    }

    @Test
    public void testEntrantFields() {
        Entrant entrant = new Entrant("E2", "Alice", "alice@mail.com", null);

        assertEquals("E2", entrant.getId());
        assertEquals("Alice", entrant.getName());
        assertEquals("alice@mail.com", entrant.getEmail());
        assertNull(entrant.getPhone());
    }
}
