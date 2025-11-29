package com.example.impact.controller;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Ensures waiting list counts are returned correctly.
 */
public class WaitingListCountTest {

    @Test
    public void fetchWaitingListCount_reportsSnapshotSize() {
        FirebaseFirestore firestore = mock(FirebaseFirestore.class, RETURNS_DEEP_STUBS);
        DocumentReference eventRef = mock(DocumentReference.class);
        CollectionReference entrants = mock(CollectionReference.class);
        Task<QuerySnapshot> task = mock(Task.class);
        QuerySnapshot snapshot = mock(QuerySnapshot.class);

        when(firestore.collection("waitingLists").document("event-1")).thenReturn(eventRef);
        when(eventRef.collection("entrants")).thenReturn(entrants);
        when(entrants.get()).thenReturn(task);
        when(task.addOnSuccessListener(any())).thenAnswer(invocation -> {
            invocation.<com.google.android.gms.tasks.OnSuccessListener<QuerySnapshot>>getArgument(0).onSuccess(snapshot);
            return task;
        });
        when(task.addOnFailureListener(any())).thenReturn(task);
        when(snapshot.size()).thenReturn(5);

        final int[] reportedCount = { -1 };
        WaitingListController controller = new WaitingListController(firestore);

        controller.fetchWaitingListCount("event-1", count -> reportedCount[0] = count, error -> { });

        assertThat(reportedCount[0], is(5));
    }
}
