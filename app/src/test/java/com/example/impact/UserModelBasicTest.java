package com.example.impact;

import com.example.impact.model.User;

import org.junit.Test;
import static org.junit.Assert.*;

public class UserModelBasicTest {

    // Dummy concrete subclass so we can instantiate the abstract User model
    static class TestUser extends User {
        public TestUser() {
            super();
        }

        public TestUser(String id, String name, String email, String phone) {
            super(id, name, email, phone);
        }

        @Override
        public String getRole() {
            return "TEST";
        }
    }

    @Test
    public void user_profileFields_setCorrectly() {
        TestUser u = new TestUser();

        u.setId("user123");
        u.setName("John Doe");
        u.setEmail("john@example.com");
        u.setPhone("1234567890");

        assertEquals("user123", u.getId());
        assertEquals("John Doe", u.getName());
        assertEquals("john@example.com", u.getEmail());
        assertEquals("1234567890", u.getPhone());
    }

    @Test
    public void user_notificationPreference_togglesCorrectly() {
        TestUser u = new TestUser();

        assertTrue(u.isNotificationsEnabled());

        u.setNotificationsEnabled(false);
        assertFalse(u.isNotificationsEnabled());

        u.setNotificationsEnabled(true);
        assertTrue(u.isNotificationsEnabled());
    }

    @Test
    public void user_constructor_setsValuesCorrectly() {
        TestUser u = new TestUser("A1", "Alice", "alice@example.com", "555-5555");

        assertEquals("A1", u.getId());
        assertEquals("Alice", u.getName());
        assertEquals("alice@example.com", u.getEmail());
        assertEquals("555-5555", u.getPhone());
    }
}
