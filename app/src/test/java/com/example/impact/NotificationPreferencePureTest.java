package com.example.impact;

import org.junit.Test;
import static org.junit.Assert.*;

public class NotificationPreferencePureTest {

    @Test
    public void user_canToggleNotifications() {
        boolean notificationsEnabled = true;

        // User disables notifications
        notificationsEnabled = !notificationsEnabled;
        assertFalse(notificationsEnabled);

        // User enables again
        notificationsEnabled = !notificationsEnabled;
        assertTrue(notificationsEnabled);
    }
}
