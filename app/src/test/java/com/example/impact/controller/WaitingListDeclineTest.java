package com.example.impact.controller;

import com.google.android.gms.tasks.Task;
import com.example.impact.controller.NotificationController;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
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
        when(entryRef.update("status", "cancelled")).thenReturn(task);
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

        verify(entryRef).update("status", "cancelled");
        assertThat(successInvoked.get(), is(true));
    }

    /**
     * Ensures a notification is emitted when the redraw flow promotes a new entrant.
     */
    @Test
    public void declineSelection_notifiesPromotedEntrant() {
        FirebaseFirestore firestore = mock(FirebaseFirestore.class, RETURNS_DEEP_STUBS);
        NotificationController notificationController = mock(NotificationController.class);
        DocumentReference entryRef = mock(DocumentReference.class);
        when(firestore.collection("waitingLists")
                .document("event-1")
                .collection("entrants")
                .document("entrant-9")).thenReturn(entryRef);

        Task<Void> task = mock(Task.class);
        when(entryRef.update("status", "cancelled")).thenReturn(task);
        when(task.addOnSuccessListener(any())).thenAnswer(invocation -> {
            invocation.<com.google.android.gms.tasks.OnSuccessListener<Void>>getArgument(0).onSuccess(null);
            return task;
        });
        when(task.addOnFailureListener(any())).thenReturn(task);

        DocumentReference promotedRef = mock(DocumentReference.class);
        Task<Void> promoteTask = mock(Task.class);
        when(promotedRef.update("status", "selected")).thenReturn(promoteTask);
        when(promoteTask.addOnSuccessListener(any())).thenAnswer(invocation -> {
            invocation.<com.google.android.gms.tasks.OnSuccessListener<Void>>getArgument(0).onSuccess(null);
            return promoteTask;
        });
        when(promoteTask.addOnFailureListener(any())).thenReturn(promoteTask);

        DocumentSnapshot promotedSnapshot = mock(DocumentSnapshot.class);
        when(promotedSnapshot.getReference()).thenReturn(promotedRef);
        when(promotedSnapshot.getString("entrantId")).thenReturn("entrant-42");
        when(promotedSnapshot.getString("eventName")).thenReturn("Hackfest");

        Task<QuerySnapshot> queryTask = mock(Task.class);
        QuerySnapshot querySnapshot = mock(QuerySnapshot.class);
        when(querySnapshot.getDocuments()).thenReturn(java.util.Collections.singletonList(promotedSnapshot));
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

        WaitingListController controller = new WaitingListController(firestore, notificationController);
        controller.declineSelection("event-1", "entrant-9", unused -> { }, error -> { });

        verify(promotedRef).update("status", "selected");
        verify(notificationController).createOfferNotification("entrant-42", "event-1", "Hackfest");
    }
}
