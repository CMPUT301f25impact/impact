package com.example.impact.controller;

import com.example.impact.model.WaitingListEntry;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Test;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Verifies waiting list controller helper behavior.
 */
public class WaitingListControllerTest {

    @Test
    public void buildWaitingListData_includesRequiredFields() {
        WaitingListController controller = new WaitingListController(mock(FirebaseFirestore.class));

        Map<String, Object> data = controller.buildWaitingListData("event-7", "Hackfest", "entrant-9");

        assertThat(data.get("eventId"), is("event-7"));
        assertThat(data.get("eventName"), is("Hackfest"));
        assertThat(data.get("entrantId"), is("entrant-9"));
        assertThat(data.get("status"), is("pending"));
        assertThat(data.get("timestamp"), is(notNullValue()));
    }

    @Test
    public void mapSnapshot_returnsEntryWhenExists() {
        DocumentSnapshot snapshot = mock(DocumentSnapshot.class);
        when(snapshot.exists()).thenReturn(true);
        WaitingListEntry entry = new WaitingListEntry();
        entry.setEventId("event-3");
        entry.setEntrantId("entrant-8");
        when(snapshot.toObject(WaitingListEntry.class)).thenReturn(entry);

        WaitingListController controller = new WaitingListController(mock(FirebaseFirestore.class));
        WaitingListEntry mapped = controller.mapSnapshot(snapshot);

        assertThat(mapped.getEventId(), is("event-3"));
        assertThat(mapped.getEntrantId(), is("entrant-8"));
    }
}
