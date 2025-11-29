package com.example.impact;

import com.example.impact.model.Event;
import com.google.firebase.firestore.DocumentSnapshot;

import org.junit.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EventTest {

    @Test
    public void testEventConstructorSetsFields() {
        Date start = new Date();
        Date end = new Date();

        Event event = new Event(
                "E100",
                "Hackathon",
                "24h coding challenge",
                start,
                end,
                "poster.png",
                Arrays.asList("coding", "tech")
        );

        assertEquals("E100", event.getId());
        assertEquals("Hackathon", event.getName());
        assertEquals("24h coding challenge", event.getDescription());
        assertEquals(start, event.getStartDate());
        assertEquals(end, event.getEndDate());
        assertEquals("poster.png", event.getPosterUrl());
        assertTrue(event.getTags().contains("coding"));
    }

    @Test
    public void testEventSettersWork() {
        Event event = new Event();

        event.setId("E200");
        event.setName("Science Fair");
        event.setDescription("Robotics and physics");
        event.setPosterUrl("image.jpg");
        event.setCapacity(100);

        assertEquals("E200", event.getId());
        assertEquals("Science Fair", event.getName());
        assertEquals("Robotics and physics", event.getDescription());
        assertEquals("image.jpg", event.getPosterUrl());
        assertEquals(Integer.valueOf(100), event.getCapacity());
    }

    @Test
    public void testNullOptionalFields() {
        Event event = new Event(
                "E300",
                "Music Fest",
                "Live music",
                null,
                null,
                null,
                null
        );

        assertNull(event.getStartDate());
        assertNull(event.getEndDate());
        assertNull(event.getPosterUrl());
        assertNotNull(event.getTags()); // tags becomes empty list, not null
    }

    @Test
    public void testOrganizerIdIsSerialized() {
        Event event = new Event();
        event.setOrganizerId("org-123");
        event.setOrganizerEmail("legacy@mail.com");

        Map<String, Object> data = event.toMap();

        assertEquals("org-123", data.get("organizerId"));
        assertEquals("legacy@mail.com", data.get("organizerEmail"));
    }

    @Test
    public void testFromSnapshotReadsOrganizerId() {
        DocumentSnapshot snapshot = mock(DocumentSnapshot.class);
        when(snapshot.toObject(Event.class)).thenReturn(new Event());
        when(snapshot.getId()).thenReturn("doc-1");
        when(snapshot.contains("posterUrl")).thenReturn(false);
        when(snapshot.contains("organizerId")).thenReturn(true);
        when(snapshot.getString("organizerId")).thenReturn("org-789");

        Event mapped = Event.fromSnapshot(snapshot);

        assertEquals("org-789", mapped.getOrganizerId());
    }
}
