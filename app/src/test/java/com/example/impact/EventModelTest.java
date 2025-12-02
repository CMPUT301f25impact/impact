package com.example.impact;

import com.example.impact.model.Event;
import org.junit.Test;
import java.util.Arrays;

import static org.junit.Assert.*;

public class EventModelTest {

    @Test
    public void event_basicFields_setCorrectly() {
        Event e = new Event();
        e.setId("E1");
        e.setName("Swim Class");
        e.setDescription("Beginners learning");

        assertEquals("E1", e.getId());
        assertEquals("Swim Class", e.getName());
        assertEquals("Beginners learning", e.getDescription());
    }

    @Test
    public void event_tags_updateCorrectly() {
        Event e = new Event();
        e.setTags(Arrays.asList("fitness", "water"));

        assertEquals(2, e.getTags().size());
        assertTrue(e.getTags().contains("fitness"));
        assertTrue(e.getTags().contains("water"));
    }
}
