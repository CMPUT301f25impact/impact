package com.example.impact;

import com.example.impact.model.Event;
import org.junit.Test;
import static org.junit.Assert.*;

public class EventQrCodeTest {

    @Test
    public void event_getQrCodePayload_returnsCorrectFormat() {
        Event e = new Event();
        e.setId("abc123");

        assertEquals("impact://event/abc123", e.getQrCodePayload());
    }
}
