package com.example.impact;

import com.example.impact.model.Admin;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Unit tests for {@link Admin} domain behavior.
 */
public class AdminTest {

    /**
     * Confirms the admin role string defaults to "admin".
     */
    @Test
    public void testAdminRoleIsCorrect() {
        Admin admin = new Admin("A1", "Carl", "carl@mail.com", "9998887777");
        assertEquals("admin", admin.getRole());
    }

    /**
     * Ensures id, name, email, and phone fields round-trip correctly.
     */
    @Test
    public void testAdminFields() {
        Admin admin = new Admin("A2", "Lisa", "lisa@mail.com", null);

        assertEquals("A2", admin.getId());
        assertEquals("Lisa", admin.getName());
        assertEquals("lisa@mail.com", admin.getEmail());
        assertNull(admin.getPhone());
    }
}
