package com.example.impact;

import com.example.impact.model.Entrant;
import com.example.impact.model.User;

import org.junit.Test;

import static org.junit.Assert.*;

public class UserTest {

    @Test
    public void testUserConstructorSetsFields() {
        User user = new Entrant("U1", "Sid", "sid@email.com", "1234567890");

        assertEquals("U1", user.getId());
        assertEquals("Sid", user.getName());
        assertEquals("sid@email.com", user.getEmail());
        assertEquals("1234567890", user.getPhone());
    }

    @Test
    public void testNullPhoneAllowed() {
        User user = new Entrant("U2", "Bob", "bob@mail.com", null);

        assertEquals("U2", user.getId());
        assertEquals("Bob", user.getName());
        assertEquals("bob@mail.com", user.getEmail());
        assertNull(user.getPhone());
    }

    @Test
    public void testSettersUpdateValues() {
        User user = new Entrant();
        user.setId("U3");
        user.setName("John");
        user.setEmail("john@mail.com");
        user.setPhone("0001112222");

        assertEquals("U3", user.getId());
        assertEquals("John", user.getName());
        assertEquals("john@mail.com", user.getEmail());
        assertEquals("0001112222", user.getPhone());
    }
}
