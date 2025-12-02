package com.example.impact;

import com.example.impact.model.Event;

import org.junit.Test;
import java.util.Arrays;
import java.util.Date;

import static org.junit.Assert.*;

public class EventBasicFieldTest {

    @Test
    public void event_settersAndGetters_workCorrectly() {
        Event e = new Event();

        e.setId("E1");
        e.setName("Dance Class");
        e.setDescription("Beginner dance safety lessons");

        Date start = new Date();
        Date end = new Date();
        e.setStartDate(start);
        e.setEndDate(end);

        e.setCapacity(20);
        e.setWaitlistCapacity(50);

        assertEquals("E1", e.getId());
        assertEquals("Dance Class", e.getName());
        assertEquals("Beginner dance safety lessons", e.getDescription());
        assertEquals(start, e.getStartDate());
        assertEquals(end, e.getEndDate());
        assertEquals(Integer.valueOf(20), e.getCapacity());
        assertEquals(Integer.valueOf(50), e.getWaitlistCapacity());
    }

    @Test
    public void event_tagsList_updatesCorrectly() {
        Event e = new Event();
        e.setTags(Arrays.asList("sports", "water"));

        assertEquals(2, e.getTags().size());
        assertTrue(e.getTags().contains("sports"));
        assertTrue(e.getTags().contains("water"));
    }
}
