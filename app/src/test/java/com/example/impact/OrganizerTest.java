package com.example.impact;

import com.example.impact.model.Organizer;
import org.junit.Test;
import static org.junit.Assert.*;

public class OrganizerTest {

    @Test
    public void testOrganizerRoleIsCorrect() {
        Organizer organizer = new Organizer("O1", "Bob", "bob@mail.com", "2223334444");
        assertEquals("organizer", organizer.getRole());
    }

    @Test
    public void testOrganizerFields() {
        Organizer organizer = new Organizer("O2", "Lily", "lily@mail.com", null);

        assertEquals("O2", organizer.getId());
        assertEquals("Lily", organizer.getName());
        assertEquals("lily@mail.com", organizer.getEmail());
        assertNull(organizer.getPhone());
    }
}
