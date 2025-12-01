package com.example.impact.controller;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Validates the lottery workflow only promotes the configured number of entrants.
 */
public class LotterySelectionTest {

    /**
     * Verifies {@link WaitingListController#runLottery} limits promotions to the requested number,
     * updates entrant statuses, and marks the lottery complete on the parent event.
     */
    @Test
    public void runLottery_updatesOnlyUpToLimitEntrants() {
        FirebaseFirestore firestore = mock(FirebaseFirestore.class, RETURNS_DEEP_STUBS);
        Task<QuerySnapshot> queryTask = mock(Task.class);
        QuerySnapshot snapshot = mock(QuerySnapshot.class);

        CollectionReference eventsCollection = mock(CollectionReference.class);
        DocumentReference eventDocument = mock(DocumentReference.class);

        when(firestore.collection("waitingLists")
                .document("event-42")
                .collection("entrants")
                .whereEqualTo("status", "pending")
                .get()).thenReturn(queryTask);
        when(firestore.collection("events")).thenReturn(eventsCollection);
        when(eventsCollection.document("event-42")).thenReturn(eventDocument);
        when(eventDocument.update("lottery_done", true)).thenReturn(mock(Task.class));
        when(queryTask.addOnSuccessListener(any())).thenAnswer(invocation -> {
            invocation.<com.google.android.gms.tasks.OnSuccessListener<QuerySnapshot>>getArgument(0).onSuccess(snapshot);
            return queryTask;
        });
        when(queryTask.addOnFailureListener(any())).thenReturn(queryTask);

        DocumentSnapshot doc1 = mock(DocumentSnapshot.class);
        DocumentSnapshot doc2 = mock(DocumentSnapshot.class);
        DocumentSnapshot doc3 = mock(DocumentSnapshot.class);
        when(snapshot.getDocuments()).thenReturn(Arrays.asList(doc1, doc2, doc3));

        DocumentReference ref1 = mock(DocumentReference.class);
        DocumentReference ref2 = mock(DocumentReference.class);
        DocumentReference ref3 = mock(DocumentReference.class);
        when(doc1.getReference()).thenReturn(ref1);
        when(doc2.getReference()).thenReturn(ref2);
        when(doc3.getReference()).thenReturn(ref3);

        WriteBatch batch = mock(WriteBatch.class);
        when(firestore.batch()).thenReturn(batch);
        when(batch.update(any(DocumentReference.class), eq("status"), eq("selected"))).thenReturn(batch);

        Task<Void> commitTask = mock(Task.class);
        when(batch.commit()).thenReturn(commitTask);
        when(commitTask.addOnSuccessListener(any())).thenAnswer(invocation -> {
            invocation.<com.google.android.gms.tasks.OnSuccessListener<Void>>getArgument(0).onSuccess(null);
            return commitTask;
        });
        when(commitTask.addOnFailureListener(any())).thenReturn(commitTask);

        AtomicBoolean successInvoked = new AtomicBoolean(false);
        WaitingListController controller = new WaitingListController(firestore);

        controller.runLottery("event-42", 2, unused -> successInvoked.set(true), error -> { });

        verify(batch, times(2)).update(any(DocumentReference.class), eq("status"), eq("selected"));
        verify(batch).commit();
        verify(eventDocument).update("lottery_done", true);
        assertThat(successInvoked.get(), is(true));
    }
}
