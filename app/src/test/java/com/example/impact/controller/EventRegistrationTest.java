package com.example.impact.controller;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifies that entrant registrations persist their data and notify callbacks.
 */
public class EventRegistrationTest {

    /**
     * Ensures registerEntrantForEvent writes to the expected document path and invokes success.
     */
    @Test
    public void registerEntrantForEvent_writesDocumentAndNotifiesSuccess() {
        FirebaseFirestore firestore = mock(FirebaseFirestore.class, RETURNS_DEEP_STUBS);
        DocumentReference eventRef = mock(DocumentReference.class);
        CollectionReference collection = mock(CollectionReference.class);
        DocumentReference registrationRef = mock(DocumentReference.class);
        Task<Void> task = mock(Task.class);

        when(firestore.collection("events").document("event-123")).thenReturn(eventRef);
        when(eventRef.collection("registeredEntrants")).thenReturn(collection);
        when(collection.document("entrant-456")).thenReturn(registrationRef);
        when(registrationRef.set(any())).thenReturn(task);
        when(task.addOnSuccessListener(any())).thenAnswer(invocation -> {
            invocation.<com.google.android.gms.tasks.OnSuccessListener<Void>>getArgument(0).onSuccess(null);
            return task;
        });
        when(task.addOnFailureListener(any())).thenReturn(task);

        AtomicBoolean successCalled = new AtomicBoolean(false);

        EventController controller = new EventController(firestore);
        controller.registerEntrantForEvent("event-123", "entrant-456", unused -> successCalled.set(true), error -> { });

        verify(registrationRef).set(any());
        assertThat(successCalled.get(), is(true));
    }
}
