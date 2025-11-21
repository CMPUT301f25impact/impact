package com.example.impact.utils.role;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.impact.model.Event;
import com.example.impact.model.Image;
import com.example.impact.model.WaitingListEntry;
import com.example.impact.utils.AppSession;
import com.example.impact.utils.FirebaseUtils;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Firestore helpers aimed at organizer-specific workflows.
 */
public final class OrganizerDb {

    private static final FirebaseFirestore db = AppSession.db();

    private OrganizerDb() {
        // no instances
    }

    public static Task<String> createEvent(@NonNull Event event) {
        TaskCompletionSource<String> tcs = new TaskCompletionSource<>();
        FirebaseUtils.createDocument("events", event,
                tcs::setResult,
                tcs::setException);
        return tcs.getTask();
    }

    public static Task<Void> updateEvent(@NonNull String eventId, @NonNull Map<String, Object> updates) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();
        FirebaseUtils.updateDocument("events", eventId, updates,
                unused -> tcs.setResult(null),
                tcs::setException);
        return tcs.getTask();
    }

    public static Task<String> uploadPoster(@NonNull String eventId, @NonNull Image image) {
        TaskCompletionSource<String> tcs = new TaskCompletionSource<>();
        image.setEventId(eventId);
        DocumentReference docRef;
        if (image.getImageId() != null && !image.getImageId().trim().isEmpty()) {
            docRef = eventImages(eventId).document(image.getImageId());
            docRef.set(image)
                    .addOnSuccessListener(unused -> tcs.setResult(docRef.getId()))
                    .addOnFailureListener(tcs::setException);
        } else {
            eventImages(eventId)
                    .add(image)
                    .addOnSuccessListener(ref -> {
                        image.setImageId(ref.getId());
                        tcs.setResult(ref.getId());
                    })
                    .addOnFailureListener(tcs::setException);
        }
        return tcs.getTask();
    }

    public static Task<List<WaitingListEntry>> fetchWaitingList(@NonNull String eventId) {
        TaskCompletionSource<List<WaitingListEntry>> tcs = new TaskCompletionSource<>();
        db.collection("events")
                .document(eventId)
                .collection("waitingList")
                .get()
                .addOnSuccessListener(snapshot -> tcs.setResult(mapWaitingList(snapshot)))
                .addOnFailureListener(tcs::setException);
        return tcs.getTask();
    }

    public static Task<Void> sendNotificationToChosen(@NonNull String eventId,
                                                      @NonNull Map<String, Object> payload) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();
        payload.putIfAbsent("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());
        db.collection("events")
                .document(eventId)
                .collection("chosen")
                .add(payload)
                .addOnSuccessListener(unused -> tcs.setResult(null))
                .addOnFailureListener(tcs::setException);
        return tcs.getTask();
    }

    public static Task<List<WaitingListEntry>> selectRandomEntrants(@NonNull String eventId, int count) {
        TaskCompletionSource<List<WaitingListEntry>> tcs = new TaskCompletionSource<>();
        fetchWaitingList(eventId)
                .addOnSuccessListener(entries -> {
                    if (entries == null || entries.isEmpty()) {
                        tcs.setResult(Collections.emptyList());
                        return;
                    }
                    Collections.shuffle(entries);
                    if (count < entries.size()) {
                        tcs.setResult(new ArrayList<>(entries.subList(0, count)));
                    } else {
                        tcs.setResult(new ArrayList<>(entries));
                    }
                })
                .addOnFailureListener(tcs::setException);
        return tcs.getTask();
    }

    private static com.google.firebase.firestore.CollectionReference eventImages(String eventId) {
        return db.collection("events").document(eventId).collection("images");
    }

    private static List<WaitingListEntry> mapWaitingList(@Nullable QuerySnapshot snapshot) {
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
