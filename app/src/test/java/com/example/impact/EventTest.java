package com.example.impact;

import com.example.impact.model.Event;
import com.google.firebase.firestore.DocumentSnapshot;

import org.junit.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests focused on the {@link Event} data model.
 */
public class EventTest {

    /**
     * Verifies the all-args constructor populates every field, including optional metadata.
     */
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
        assertFalse(event.isLottery_done());
    }

    /**
     * Ensures the setter methods override default field values.
     */
    @Test
    public void testEventSettersWork() {
        Event event = new Event();

        event.setId("E200");
        event.setName("Science Fair");
        event.setDescription("Robotics and physics");
        event.setPosterUrl("image.jpg");
        event.setCapacity(100);
        event.setLottery_done(true);

        assertEquals("E200", event.getId());
        assertEquals("Science Fair", event.getName());
        assertEquals("Robotics and physics", event.getDescription());
        assertEquals("image.jpg", event.getPosterUrl());
        assertEquals(Integer.valueOf(100), event.getCapacity());
        assertTrue(event.isLottery_done());
    }

    /**
     * Checks that nullable constructor arguments remain null and tags default to an empty list.
     */
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

    /**
     * Confirms organizer credentials are serialized when writing event data.
     */
    @Test
    public void testOrganizerIdIsSerialized() {
        Event event = new Event();
        event.setOrganizerId("org-123");
        event.setOrganizerEmail("legacy@mail.com");

        Map<String, Object> data = event.toMap();

        assertEquals("org-123", data.get("organizerId"));
        assertEquals("legacy@mail.com", data.get("organizerEmail"));
    }

    /**
     * Validates {@link Event#fromSnapshot(DocumentSnapshot)} reads optional organizer metadata.
     */
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
