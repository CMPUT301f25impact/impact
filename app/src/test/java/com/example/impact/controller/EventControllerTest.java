package com.example.impact.controller;

import com.example.impact.model.Event;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
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

    @Test
    public void fetchEventById_invokesSuccessWithMappedEvent() {
        FirebaseFirestore firestore = mock(FirebaseFirestore.class, org.mockito.Mockito.RETURNS_DEEP_STUBS);
        DocumentReference documentReference = mock(DocumentReference.class);
        when(firestore.collection("events").document("event-5")).thenReturn(documentReference);

        @SuppressWarnings("unchecked")
        Task<DocumentSnapshot> task = (Task<DocumentSnapshot>) mock(Task.class);
        when(documentReference.get()).thenReturn(task);

        DocumentSnapshot snapshot = mock(DocumentSnapshot.class);
        Event expected = new Event("event-5", "Demo", "Desc", new Date(), null, null, null);
        when(snapshot.exists()).thenReturn(true);
        when(snapshot.toObject(Event.class)).thenReturn(expected);
        when(snapshot.getId()).thenReturn("event-5");

        when(task.addOnSuccessListener(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> {
            invocation.<com.google.android.gms.tasks.OnSuccessListener<DocumentSnapshot>>getArgument(0).onSuccess(snapshot);
            return task;
        });
        when(task.addOnFailureListener(org.mockito.ArgumentMatchers.any())).thenReturn(task);

        EventController controller = new EventController(firestore);
        AtomicReference<Event> captured = new AtomicReference<>();
        controller.fetchEventById("event-5", captured::set, null);

        assertNotNull(captured.get());
        assertThat(captured.get().getId(), is("event-5"));
    }
}
