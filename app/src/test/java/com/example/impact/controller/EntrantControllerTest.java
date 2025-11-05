package com.example.impact.controller;

import com.example.impact.model.Entrant;
import com.example.impact.model.EntrantHistoryItem;
import com.example.impact.model.WaitingListEntry;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests covering basic entrant controller validation and mapping logic.
 */
public class EntrantControllerTest {

    @Test
    public void buildEntrantData_includesCoreFields() {
        Entrant entrant = new Entrant("id-123", "Sam Sample", "sam@example.com", null);

        Map<String, Object> data = EntrantController.buildEntrantData(entrant);

        assertThat(data.get("id"), is("id-123"));
        assertThat(data.get("name"), is("Sam Sample"));
        assertThat(data.get("email"), is("sam@example.com"));
        assertThat(data.get("phone"), is(nullValue()));
    }

    @Test
    public void buildEntrantData_includesPhoneWhenPresent() {
        Entrant entrant = new Entrant("id-456", "Alex Example", "alex@example.com", "7801234567");

        Map<String, Object> data = EntrantController.buildEntrantData(entrant);

        assertThat(data.get("phone"), is("7801234567"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateEntrant_rejectsMissingName() {
        Entrant entrant = new Entrant("id-456", "", "alex@example.com", null);

        EntrantController.validateEntrant(entrant);
    }

    @Test
    public void mapHistory_ordersByMostRecent() {
        WaitingListEntry older = new WaitingListEntry("eventA", "Event A", "entrant", new Date(1000), "selected");
        WaitingListEntry newer = new WaitingListEntry("eventB", "Event B", "entrant", new Date(5000), "not selected");

        DocumentSnapshot olderSnapshot = mock(DocumentSnapshot.class);
        when(olderSnapshot.exists()).thenReturn(true);
        when(olderSnapshot.toObject(WaitingListEntry.class)).thenReturn(older);

        DocumentSnapshot newerSnapshot = mock(DocumentSnapshot.class);
        when(newerSnapshot.exists()).thenReturn(true);
        when(newerSnapshot.toObject(WaitingListEntry.class)).thenReturn(newer);

        QuerySnapshot querySnapshot = mock(QuerySnapshot.class);
        when(querySnapshot.getDocuments()).thenReturn(Arrays.asList(olderSnapshot, newerSnapshot));

        EntrantController controller = new EntrantController(mock(com.google.firebase.firestore.FirebaseFirestore.class));
        List<EntrantHistoryItem> items = controller.mapHistory(querySnapshot);

        assertThat(items.get(0).getEventId(), is("eventB"));
        assertThat(items.get(1).getEventId(), is("eventA"));
    }
}
