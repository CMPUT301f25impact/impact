package com.example.impact;

import com.example.impact.model.Event;

import org.junit.Test;

import java.util.Arrays;
import java.util.Date;

import static org.junit.Assert.*;

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
}
