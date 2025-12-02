package com.example.impact.controller;

import static org.junit.Assert.*;

import com.example.impact.model.Notification;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

/**
 * A simple test verifying that buildNotificationData() correctly
 * maps a Notification into a Firestore payload.
 *
 * This avoids reflection, avoids Firestore calls,
 * and is guaranteed to pass on local + GitHub Actions.
 */
public class NotificationControllerSimpleTest {

    @Test
    public void buildNotificationData_basicFieldsAreMapped() {
        // Arrange
        FirebaseFirestore mockDb = Mockito.mock(FirebaseFirestore.class);
        NotificationController controller = new NotificationController(mockDb);

        Notification notification = new Notification(
                "notif123",
                null, // sender
                new ArrayList<>(), // recipients
                null, // related_event
                "Hello!",
                new Date(123456) // timestamp
        );

        // Act
        Map<String, Object> data = NotificationController.buildNotificationData(notification);

        // Assert
        assertEquals("notif123", data.get("id"));
        assertEquals("Hello!", data.get("message"));
        assertTrue(data.containsKey("recipients"));
        assertTrue(data.containsKey("related_event"));
        assertTrue(data.containsKey("time_stamp"));
    }
}
