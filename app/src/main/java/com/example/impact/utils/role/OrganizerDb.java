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
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Centralizes all organizer-facing Firestore interactions.
 * <p>
 * Every helper in this class targets {@code events/{eventId}} subtrees by either mutating the
 * event document itself or interacting with nested collections such as {@code images/},
 * {@code waitingList/}, or {@code chosen/}. Using these helpers keeps path construction out of
 * controllers and fragments.
 */
public final class OrganizerDb {

    private static final FirebaseFirestore db = AppSession.db();

    private OrganizerDb() {
        // no instances
    }

    /**
     * Creates a new event document under {@code events/}.
     *
     * @param event populated {@link Event} instance to persist
     * @return task containing the generated document id
     */
    public static Task<String> createEvent(@NonNull Event event) {
        TaskCompletionSource<String> tcs = new TaskCompletionSource<>();
        FirebaseUtils.createDocument("events", event,
                tcs::setResult,
                tcs::setException);
        return tcs.getTask();
    }

    /**
     * Applies partial updates to {@code events/{eventId}}.
     *
     * @param eventId Firestore identifier of the event
     * @param updates field-value map passed to {@link FirebaseUtils#updateDocument}
     * @return task that resolves when the update completes
     */
    public static Task<Void> updateEvent(@NonNull String eventId, @NonNull Map<String, Object> updates) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();
        FirebaseUtils.updateDocument("events", eventId, updates,
                unused -> tcs.setResult(null),
                tcs::setException);
        return tcs.getTask();
    }

    /**
     * Writes an image document under {@code events/{eventId}/images/}. If {@link Image#getImageId()}
     * is provided it will overwrite that document, otherwise a new document is created.
     *
     * @param eventId parent event identifier
     * @param image image metadata/content
     * @return task resolving to the stored image document id
     */
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

    /**
     * Loads every waiting-list entry under {@code events/{eventId}/waitingList/}.
     *
     * @param eventId parent event identifier
     * @return task yielding mapped {@link WaitingListEntry} instances
     */
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

    /**
     * Loads every image stored in {@code events/{eventId}/images/}.
     *
     * @param eventId event whose assets should be returned
     * @return task with the decoded {@link Image} list
     */
    public static Task<List<Image>> fetchEventImages(@NonNull String eventId) {
        TaskCompletionSource<List<Image>> tcs = new TaskCompletionSource<>();
        eventImages(eventId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Image> images = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        images.add(Image.fromSnapshot(doc));
                    }
                    tcs.setResult(images);
                })
                .addOnFailureListener(tcs::setException);
        return tcs.getTask();
    }

    /**
     * Appends a notification payload under {@code events/{eventId}/chosen/}.
     * The payload automatically receives a {@code timestamp} if one is missing.
     *
     * @param eventId event issuing the notification
     * @param payload arbitrary key/value pairs stored in the chosen subcollection
     * @return task that completes once the document is written
     */
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

    /**
     * Shuffles the waiting-list entries for an event and returns up to {@code count} of them.
     * This helper is useful when organizers need to select random winners locally.
     *
     * @param eventId parent event identifier
     * @param count number of entrants to select (bounded by list size)
     * @return task with the randomly selected entries
     */
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

    /**
     * Queries {@code events/} for rows whose {@code organizerEmail} matches the supplied value.
     *
     * @param organizerEmail organizer address tied to the events
     * @return task resolving with the organizer's event list
     */
    public static Task<List<Event>> fetchEventsByEmail(@NonNull String organizerEmail) {
        TaskCompletionSource<List<Event>> tcs = new TaskCompletionSource<>();
        db.collection("events")
                .whereEqualTo("organizerEmail", organizerEmail)
                .get()
                .addOnSuccessListener(snapshot -> tcs.setResult(mapEvents(snapshot)))
                .addOnFailureListener(tcs::setException);
        return tcs.getTask();
    }

    /**
     * Subscribes to realtime updates for events owned by the provided email address.
     *
     * @param organizerEmail organizer address used in the equality filter
     * @param listener Firestore listener receiving {@link QuerySnapshot} updates
     * @return {@link ListenerRegistration} that can be removed when the UI stops listening
     */
    public static ListenerRegistration listenToEventsByEmail(@NonNull String organizerEmail,
                                                             @NonNull EventListener<QuerySnapshot> listener) {
        return db.collection("events")
                .whereEqualTo("organizerEmail", organizerEmail)
                .addSnapshotListener(listener);
    }

    /**
     * Subscribes to realtime waiting list changes for an event. Documents are returned in descending
     * {@code timestamp} order so most recent actions appear first in the UI.
     *
     * @param eventId event identifier
     * @param listener callback invoked with snapshot updates
     * @return handle that can be removed to stop listening
     */
    public static ListenerRegistration listenToWaitingList(@NonNull String eventId,
                                                           @NonNull EventListener<QuerySnapshot> listener) {
        return db.collection("events")
                .document(eventId)
                .collection("waitingList")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener(listener);
    }

    /**
     * Deletes {@code events/{eventId}} on behalf of the organizer.
     *
     * @param eventId Firestore identifier to remove
     * @return task that resolves when deletion succeeds
     */
    public static Task<Void> deleteEvent(@NonNull String eventId) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();
        FirebaseUtils.deleteDocument("events", eventId,
                unused -> tcs.setResult(null),
                tcs::setException);
        return tcs.getTask();
    }

    /**
     * Forcibly clears {@code deviceId} references for any organizer accounts bound to the device.
     *
     * @param deviceId Android device identifier
     * @return task that completes after all matching documents are updated
     */
    public static Task<Void> clearDeviceBinding(@NonNull String deviceId) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();
        db.collection("users")
                .whereEqualTo("deviceId", deviceId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        doc.getReference().update("deviceId", null);
                    }
                    tcs.setResult(null);
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

    private static List<Event> mapEvents(@Nullable QuerySnapshot snapshot) {
        List<Event> events = new ArrayList<>();
        if (snapshot == null) {
            return events;
        }
        for (DocumentSnapshot doc : snapshot.getDocuments()) {
            events.add(Event.fromSnapshot(doc));
        }
        return events;
    }
}
