package com.example.impact.controller;

import com.example.impact.model.Event;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Exercises the helper methods within {@link EventController} for safe mapping.
 */
public class EventControllerTest {

    @Test
    public void mapEvents_extractsEvents() {
        Event first = new Event("event-1", "Hackathon", "Build apps", new Date(), null, null, null);
        Event second = new Event("event-2", "Workshop", "Learn skills", new Date(), null, null, null);

        DocumentSnapshot firstSnapshot = mock(DocumentSnapshot.class);
        when(firstSnapshot.toObject(Event.class)).thenReturn(first);
        when(firstSnapshot.getId()).thenReturn("event-1");

        DocumentSnapshot secondSnapshot = mock(DocumentSnapshot.class);
        when(secondSnapshot.toObject(Event.class)).thenReturn(second);
        when(secondSnapshot.getId()).thenReturn("event-2");

        QuerySnapshot querySnapshot = mock(QuerySnapshot.class);
        when(querySnapshot.getDocuments()).thenReturn(Arrays.asList(firstSnapshot, secondSnapshot));

        EventController controller = new EventController(mock(FirebaseFirestore.class));
        List<Event> events = controller.mapEvents(querySnapshot);

        assertThat(events.size(), is(2));
        assertThat(events.get(0).getId(), is("event-1"));
        assertThat(events.get(1).getId(), is("event-2"));
    }
}
