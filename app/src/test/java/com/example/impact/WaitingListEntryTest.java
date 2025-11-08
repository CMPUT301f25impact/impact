package com.example.impact;

import com.example.impact.model.WaitingListEntry;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

public class WaitingListEntryTest {

    @Test
    public void testWaitingListEntryConstructor() {
        Date now = new Date();

        WaitingListEntry entry = new WaitingListEntry(
                "E1",
                "Science Fair",
                "U10",
                now,
                "Joined"
        );

        assertEquals("E1", entry.getEventId());
        assertEquals("Science Fair", entry.getEventName());
        assertEquals("U10", entry.getEntrantId());
        assertEquals("Joined", entry.getStatus());
        assertEquals(now, entry.getTimestamp());
    }

    @Test
    public void testWaitingListEntrySetters() {
        WaitingListEntry entry = new WaitingListEntry();
        Date d = new Date();

        entry.setEventId("E9");
        entry.setEventName("Music Fest");
        entry.setEntrantId("U99");
        entry.setStatus("Pending");
        entry.setTimestamp(d);

        assertEquals("E9", entry.getEventId());
        assertEquals("Music Fest", entry.getEventName());
        assertEquals("U99", entry.getEntrantId());
        assertEquals("Pending", entry.getStatus());
        assertEquals(d, entry.getTimestamp());
    }
}
