package com.example.impact.controller;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.Test;

import java.util.Arrays;
import java.util.Date;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Covers deterministic replacement selection when a selected entrant declines.
 */
public class ReplacementSelectionTest {

    /**
     * Ensures that the controller promotes the entrant with the earliest timestamp.
     */
    @Test
    public void selectNextPending_prefersEarliestTimestamp() {
        DocumentSnapshot oldest = mock(DocumentSnapshot.class);
        when(oldest.getTimestamp("timestamp")).thenReturn(new Timestamp(new Date(1_000)));

        DocumentSnapshot newest = mock(DocumentSnapshot.class);
        when(newest.getTimestamp("timestamp")).thenReturn(new Timestamp(new Date(5_000)));

        DocumentSnapshot missingTs = mock(DocumentSnapshot.class);
        when(missingTs.getTimestamp("timestamp")).thenReturn(null);

        QuerySnapshot snapshot = mock(QuerySnapshot.class);
        when(snapshot.getDocuments()).thenReturn(Arrays.asList(newest, missingTs, oldest));

        WaitingListController controller = new WaitingListController(mock(FirebaseFirestore.class));

        DocumentSnapshot selected = controller.selectNextPending(snapshot);

        assertThat(selected, is(oldest));
    }
}
