package com.example.impact.controller;

import com.google.android.gms.tasks.Task;
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
 * Verifies the acceptSelection workflow updates Firestore and invokes callbacks.
 */
public class WaitingListAcceptanceTest {

    /**
     * Ensures acceptSelection writes the accepted status and triggers the success callback.
     */
    @Test
    public void acceptSelection_updatesStatusAndNotifiesSuccess() {
        FirebaseFirestore firestore = mock(FirebaseFirestore.class, RETURNS_DEEP_STUBS);
        DocumentReference entryRef = mock(DocumentReference.class);
        when(firestore.collection("waitingLists")
                .document("event-1")
                .collection("entrants")
                .document("entrant-9")).thenReturn(entryRef);

        Task<Void> task = mock(Task.class);
        when(entryRef.update("status", "accepted")).thenReturn(task);
        when(task.addOnSuccessListener(any())).thenAnswer(invocation -> {
            invocation.<com.google.android.gms.tasks.OnSuccessListener<Void>>getArgument(0).onSuccess(null);
            return task;
        });
        when(task.addOnFailureListener(any())).thenReturn(task);

        AtomicBoolean successInvoked = new AtomicBoolean(false);
        WaitingListController controller = new WaitingListController(firestore);

        controller.acceptSelection("event-1", "entrant-9", unused -> successInvoked.set(true), error -> { });

        verify(entryRef).update("status", "accepted");
        assertThat(successInvoked.get(), is(true));
    }
}
