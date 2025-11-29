package com.example.impact.controller;

import com.example.impact.model.Entrant;

import org.junit.Test;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tests covering the entrant notification preference behavior.
 */
public class NotificationPreferenceTest {

    /**
     * Verifies that a newly constructed entrant opts into notifications by default.
     */
    @Test
    public void entrantDefaultsToNotificationsEnabled() {
        Entrant entrant = new Entrant("entrant-id", "Test Entrant", "entrant@example.com", null);

        assertThat(entrant.isNotificationsEnabled(), is(true));
    }

    /**
     * Ensures that the notification preference is serialized into the Firestore payload.
     */
    @Test
    public void buildUserData_persistsNotificationPreference() {
        Entrant entrant = new Entrant("entrant-id", "Test Entrant", "entrant@example.com", null);
        entrant.setNotificationsEnabled(false);

        Map<String, Object> userData = UserController.buildUserData(entrant);

        assertThat(userData.get("notificationsEnabled"), is(false));
    }
}
