package com.example.impact;

import com.example.impact.model.WaitingListEntry;

import org.junit.Test;
import java.util.Date;

import static org.junit.Assert.*;

public class WaitingListEntryBasicTest {

    @Test
    public void waitingListEntry_constructor_setsFields() {
        String eventId = "event123";
        String eventName = "Swim Lessons";
        String entrantId = "user999";
        Date now = new Date();
        String status = "PENDING";

        WaitingListEntry entry = new WaitingListEntry(eventId, eventName, entrantId, now, status);

        assertEquals(eventId, entry.getEventId());
        assertEquals(eventName, entry.getEventName());
        assertEquals(entrantId, entry.getEntrantId());
        assertEquals(now, entry.getTimestamp());
        assertEquals(status, entry.getStatus());
    }
}
