package com.example.impact.controller;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

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
 * Validates the declineSelection workflow updates status and notifies success listeners.
 */
public class WaitingListDeclineTest {

    /**
     * Confirms declineSelection writes "not selected" and triggers the success callback.
     */
    @Test
    public void declineSelection_updatesStatusAndNotifiesSuccess() {
        FirebaseFirestore firestore = mock(FirebaseFirestore.class, RETURNS_DEEP_STUBS);
        DocumentReference entryRef = mock(DocumentReference.class);
        when(firestore.collection("waitingLists")
                .document("event-1")
                .collection("entrants")
                .document("entrant-9")).thenReturn(entryRef);

        Task<Void> task = mock(Task.class);
        when(entryRef.update("status", "not selected")).thenReturn(task);
        when(task.addOnSuccessListener(any())).thenAnswer(invocation -> {
            invocation.<com.google.android.gms.tasks.OnSuccessListener<Void>>getArgument(0).onSuccess(null);
            return task;
        });
        when(task.addOnFailureListener(any())).thenReturn(task);

        Task<QuerySnapshot> queryTask = mock(Task.class);
        QuerySnapshot querySnapshot = mock(QuerySnapshot.class);
        when(querySnapshot.getDocuments()).thenReturn(java.util.Collections.emptyList());
        when(firestore.collection("waitingLists")
                .document("event-1")
                .collection("entrants")
                .whereEqualTo("status", "pending")
                .get()).thenReturn(queryTask);
        when(queryTask.addOnSuccessListener(any())).thenAnswer(invocation -> {
            invocation.<com.google.android.gms.tasks.OnSuccessListener<QuerySnapshot>>getArgument(0).onSuccess(querySnapshot);
            return queryTask;
        });
        when(queryTask.addOnFailureListener(any())).thenReturn(queryTask);

        AtomicBoolean successInvoked = new AtomicBoolean(false);
        WaitingListController controller = new WaitingListController(firestore);

        controller.declineSelection("event-1", "entrant-9", unused -> successInvoked.set(true), error -> { });

        verify(entryRef).update("status", "not selected");
        assertThat(successInvoked.get(), is(true));
    }
}
