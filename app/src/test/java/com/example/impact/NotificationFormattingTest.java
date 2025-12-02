package com.example.impact;

import com.example.impact.model.Notification;
import com.example.impact.model.User;
import com.example.impact.model.Event;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class NotificationFormattingTest {

    @Test
    public void notification_constructor_setsFieldsCorrectly() {

        User sender = mock(User.class);
        ArrayList<User> recipients = new ArrayList<>();
        Event evt = mock(Event.class);

        Date timestamp = new Date(1234L);

        Notification n = new Notification(
                "N1",
                sender,
                recipients,
                evt,
                "You have been selected!",
                timestamp
        );

        assertEquals("N1", n.getId());
        assertEquals(sender, n.getSender());
        assertEquals(recipients, n.getRecipients());
        assertEquals(evt, n.getRelated_event());
        assertEquals("You have been selected!", n.getMessage());
        assertEquals(timestamp, n.getTime_stamp());
        assertEquals(timestamp, n.getCreatedAt()); // alias accessor
    }

    @Test
    public void notification_setMessage_updatesCorrectly() {
        Notification n = new Notification();
        n.setMessage("Hello Entrant!");
        assertEquals("Hello Entrant!", n.getMessage());
    }

    @Test
    public void notification_eventName_returnsCorrectly() {
        // Set up fake event
        Event evt = mock(Event.class);
        when(evt.getName()).thenReturn("Dance Class");

        Notification n = new Notification(
                "N2",
                null,
                new ArrayList<>(),
                evt,
                "msg"
        );

        assertEquals("Dance Class", n.getEventName());
    }

    @Test
    public void notification_eventName_nullWhenNoEvent() {
        Notification n = new Notification();
        assertNull(n.getEventName());
    }
}
