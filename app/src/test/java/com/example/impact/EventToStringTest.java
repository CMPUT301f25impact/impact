package com.example.impact;

import com.example.impact.model.Event;
import org.junit.Test;
import static org.junit.Assert.*;

public class EventToStringTest {

    @Test
    public void event_toString_returnsName() {
        Event e = new Event();
        e.setName("Swim Lessons");

        assertEquals("Swim Lessons", e.toString());
    }
}
