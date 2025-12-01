package com.example.impact;

import com.example.impact.model.Organizer;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Unit tests for the {@link Organizer} user type.
 */
public class OrganizerTest {

    /**
     * Validates organizers report their role as "organizer".
     */
    @Test
    public void testOrganizerRoleIsCorrect() {
        Organizer organizer = new Organizer("O1", "Bob", "bob@mail.com", "2223334444");
        assertEquals("organizer", organizer.getRole());
    }

    /**
     * Ensures constructor arguments populate the identifying fields.
     */
    @Test
    public void testOrganizerFields() {
        Organizer organizer = new Organizer("O2", "Lily", "lily@mail.com", null);

        assertEquals("O2", organizer.getId());
        assertEquals("Lily", organizer.getName());
        assertEquals("lily@mail.com", organizer.getEmail());
        assertNull(organizer.getPhone());
    }
}
