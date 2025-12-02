package com.example.impact;

import com.example.impact.model.Notification;
import org.junit.Test;
import static org.junit.Assert.*;

public class NotificationGetEventNameTest {

    @Test
    public void notification_getEventName_nullEvent_returnsNull() {
        Notification n = new Notification();
        assertNull(n.getEventName());
    }
}
