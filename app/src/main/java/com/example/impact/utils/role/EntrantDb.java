package com.example.impact.utils.role;

import androidx.annotation.NonNull;

import com.example.impact.model.Event;
import com.example.impact.model.WaitingListEntry;
import com.example.impact.utils.AppSession;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helpers for entrant-centric Firestore operations.
 */
public final class EntrantDb {

    private static final FirebaseFirestore db = AppSession.db();

    private EntrantDb() {
        // utility
    }

    public static Task<Void> joinWaitingList(@NonNull String eventId, @NonNull String entrantId) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();
        db.collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    String eventName = snapshot.getString("name");
                    Map<String, Object> data = new HashMap<>();
                    data.put("eventId", eventId);
                    data.put("eventName", eventName);
                    data.put("entrantId", entrantId);
                    data.put("status", "pending");
                    data.put("timestamp", FieldValue.serverTimestamp());
                    snapshot.getReference()
                            .collection("waitingList")
                            .document(entrantId)
                            .set(data)
                            .addOnSuccessListener(unused -> tcs.setResult(null))
                            .addOnFailureListener(tcs::setException);
                })
                .addOnFailureListener(tcs::setException);
        return tcs.getTask();
    }

    public static Task<Void> leaveWaitingList(@NonNull String eventId, @NonNull String entrantId) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();
        db.collection("events")
                .document(eventId)
                .collection("waitingList")
                .document(entrantId)
                .delete()
                .addOnSuccessListener(unused -> tcs.setResult(null))
                .addOnFailureListener(tcs::setException);
        return tcs.getTask();
    }

    public static Task<List<WaitingListEntry>> getEventHistory(@NonNull String entrantId) {
        TaskCompletionSource<List<WaitingListEntry>> tcs = new TaskCompletionSource<>();
        db.collectionGroup("waitingList")
                .whereEqualTo("entrantId", entrantId)
                .get()
                .addOnSuccessListener(snapshot -> tcs.setResult(mapEntries(snapshot)))
                .addOnFailureListener(tcs::setException);
        return tcs.getTask();
    }

    public static Task<Event> fetchEventDetails(@NonNull String eventId) {
        TaskCompletionSource<Event> tcs = new TaskCompletionSource<>();
        db.collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(snapshot -> tcs.setResult(Event.fromSnapshot(snapshot)))
                .addOnFailureListener(tcs::setException);
        return tcs.getTask();
    }

    private static List<WaitingListEntry> mapEntries(QuerySnapshot snapshot) {
        List<WaitingListEntry> entries = new ArrayList<>();
        if (snapshot == null) {
            return entries;
        }
        for (DocumentSnapshot doc : snapshot.getDocuments()) {
            entries.add(WaitingListEntry.fromSnapshot(doc));
        }
        return entries;
    }
}
