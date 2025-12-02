package com.example.impact;

import com.example.impact.model.Event;

import org.junit.Test;
import java.util.Date;

import static org.junit.Assert.*;

public class EventRegistrationDateTest {

    @Test
    public void event_start_and_end_dates_setCorrectly() {

        Event event = new Event();

        Date start = new Date(1000L);
        Date end   = new Date(2000L);

        event.setStartDate(start);
        event.setEndDate(end);

        assertEquals(start, event.getStartDate());
        assertEquals(end, event.getEndDate());
    }

    @Test
    public void event_start_and_end_dates_acceptNull() {

        Event event = new Event();

        event.setStartDate(null);
        event.setEndDate(null);

        assertNull(event.getStartDate());
        assertNull(event.getEndDate());
    }
}
