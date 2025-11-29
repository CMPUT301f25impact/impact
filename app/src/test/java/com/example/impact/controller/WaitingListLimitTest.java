package com.example.impact.controller;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Ensures the waiting list controller respects organizer-defined limits.
 */
public class WaitingListLimitTest {

    @Test
    public void joinWaitingList_stopsWhenLimitReached() {
        FirebaseFirestore firestore = mock(FirebaseFirestore.class);
        CollectionReference eventsCollection = mock(CollectionReference.class);
        DocumentReference eventDocument = mock(DocumentReference.class);
        Task<DocumentSnapshot> eventTask = mock(Task.class);

        when(firestore.collection("events")).thenReturn(eventsCollection);
        when(eventsCollection.document("event-1")).thenReturn(eventDocument);
        when(eventDocument.get()).thenReturn(eventTask);

        final OnSuccessListener<DocumentSnapshot>[] eventSuccessHolder = new OnSuccessListener[1];
        doAnswer(invocation -> {
            eventSuccessHolder[0] = invocation.getArgument(0);
            return eventTask;
        }).when(eventTask).addOnSuccessListener(any());
        when(eventTask.addOnFailureListener(any())).thenReturn(eventTask);

        CollectionReference waitingLists = mock(CollectionReference.class);
        DocumentReference waitingListDocument = mock(DocumentReference.class);
        CollectionReference entrantsCollection = mock(CollectionReference.class);
        DocumentReference entrantDocument = mock(DocumentReference.class);
        Task<QuerySnapshot> entrantsTask = mock(Task.class);

        when(firestore.collection("waitingLists")).thenReturn(waitingLists);
        when(waitingLists.document("event-1")).thenReturn(waitingListDocument);
        when(waitingListDocument.collection("entrants")).thenReturn(entrantsCollection);
        when(entrantsCollection.document("entrant-9")).thenReturn(entrantDocument);
        when(entrantsCollection.get()).thenReturn(entrantsTask);

        final OnSuccessListener<QuerySnapshot>[] entrantsSuccessHolder = new OnSuccessListener[1];
        doAnswer(invocation -> {
            entrantsSuccessHolder[0] = invocation.getArgument(0);
            return entrantsTask;
        }).when(entrantsTask).addOnSuccessListener(any());
        when(entrantsTask.addOnFailureListener(any())).thenReturn(entrantsTask);

        WaitingListController controller = new WaitingListController(firestore);
        OnSuccessListener<Void> successListener = mock(OnSuccessListener.class);
        OnFailureListener failureListener = mock(OnFailureListener.class);

        controller.joinWaitingList("event-1", "Demo", "entrant-9", successListener, failureListener);

        DocumentSnapshot eventSnapshot = mock(DocumentSnapshot.class);
        when(eventSnapshot.getLong("maxEntrants")).thenReturn(2L);
        eventSuccessHolder[0].onSuccess(eventSnapshot);

        QuerySnapshot entrantSnapshot = mock(QuerySnapshot.class);
        DocumentSnapshot existingOne = mock(DocumentSnapshot.class);
        when(existingOne.getId()).thenReturn("entrant-1");
        DocumentSnapshot existingTwo = mock(DocumentSnapshot.class);
        when(existingTwo.getId()).thenReturn("entrant-2");
        when(entrantSnapshot.size()).thenReturn(2);
        when(entrantSnapshot.getDocuments()).thenReturn(Arrays.asList(existingOne, existingTwo));
        entrantsSuccessHolder[0].onSuccess(entrantSnapshot);

        ArgumentCaptor<Exception> exceptionCaptor = ArgumentCaptor.forClass(Exception.class);
        verify(failureListener).onFailure(exceptionCaptor.capture());
        assertThat(exceptionCaptor.getValue().getMessage(), is(WaitingListController.ERROR_WAITING_LIST_LIMIT_REACHED));
        verify(successListener, never()).onSuccess(any());
        verify(entrantDocument, never()).set(any());
    }
}
