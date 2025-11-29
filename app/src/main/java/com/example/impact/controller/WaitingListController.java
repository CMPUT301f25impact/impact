package com.example.impact.controller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.impact.model.WaitingListEntry;
import com.example.impact.utils.AppSession;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.Date;

/**
 * Handles operations for joining or leaving event waiting lists.
 */
public class WaitingListController {
    private static final String COLLECTION_WAITING_LISTS = "waitingLists";
    private static final String SUB_COLLECTION_ENTRANTS = "entrants";

    private static final String STATUS_PENDING = "pending";
    private static final String STATUS_SELECTED = "selected";
    private static final String STATUS_ACCEPTED = "accepted";
    private static final String STATUS_NOT_SELECTED = "not selected";

    private final FirebaseFirestore firestore;

    /**
     * Creates a controller backed by the default Firestore instance.
     */
    public WaitingListController() {
        this(AppSession.db());
    }

    /**
     * Creates a controller with an injected Firestore instance.
     *
     * @param firestore shared Firestore reference
     */
    public WaitingListController(@NonNull FirebaseFirestore firestore) {
        this.firestore = firestore;
    }

    /**
     * Adds the entrant to the waiting list for the specified event.
     *
     * @param eventId         event identifier
     * @param eventName       friendly name stored with the entry
     * @param entrantId       entrant identifier
     * @param successListener invoked when the write succeeds
     * @param failureListener invoked when the write fails
     */
    public void joinWaitingList(@NonNull String eventId,
                                @NonNull String eventName,
                                @NonNull String entrantId,
                                @Nullable OnSuccessListener<Void> successListener,
                                @Nullable OnFailureListener failureListener) {
        validateIds(eventId, entrantId);

        Map<String, Object> data = buildWaitingListData(eventId, eventName, entrantId);
        Task<Void> task = firestore.collection(COLLECTION_WAITING_LISTS)
                .document(eventId)
                .collection(SUB_COLLECTION_ENTRANTS)
                .document(entrantId)
                .set(data);

        attachListeners(task, successListener, failureListener);
    }

    /**
     * Removes the entrant from the waiting list.
     *
     * @param eventId         event identifier
     * @param entrantId       entrant identifier
     * @param successListener invoked when the delete succeeds
     * @param failureListener invoked when the delete fails
     */
    public void leaveWaitingList(@NonNull String eventId,
                                 @NonNull String entrantId,
                                 @Nullable OnSuccessListener<Void> successListener,
                                 @Nullable OnFailureListener failureListener) {
        validateIds(eventId, entrantId);

        Task<Void> task = firestore.collection(COLLECTION_WAITING_LISTS)
                .document(eventId)
                .collection(SUB_COLLECTION_ENTRANTS)
                .document(entrantId)
                .delete();

        attachListeners(task, successListener, failureListener);
    }

    /**
     * Records a decline for a selected entrant, marking their entry as {@code "not selected"},
     * and promotes the next eligible entrant when possible.
     *
     * @param eventId         event identifier
     * @param entrantId       entrant identifier
     * @param successListener invoked once the replacement flow completes (even when no replacement exists)
     * @param failureListener invoked when either decline or replacement writes fail
     */
    public void declineSelection(@NonNull String eventId,
                                 @NonNull String entrantId,
                                 @Nullable OnSuccessListener<Void> successListener,
                                 @Nullable OnFailureListener failureListener) {
        validateIds(eventId, entrantId);

        DocumentReference entryRef = firestore.collection(COLLECTION_WAITING_LISTS)
                .document(eventId)
                .collection(SUB_COLLECTION_ENTRANTS)
                .document(entrantId);

        entryRef.update("status", STATUS_NOT_SELECTED)
                .addOnSuccessListener(v -> promoteNextEntrant(eventId, successListener, failureListener))
                .addOnFailureListener(error -> {
                    if (failureListener != null) {
                        failureListener.onFailure(error);
                    }
                });
    }

    /**
     * Marks a selected entrant as having accepted their invitation.
     *
     * @param eventId         event identifier
     * @param entrantId       entrant identifier
     * @param successListener invoked when the write succeeds
     * @param failureListener invoked when the write fails
     */
    public void acceptSelection(@NonNull String eventId,
                                @NonNull String entrantId,
                                @Nullable OnSuccessListener<Void> successListener,
                                @Nullable OnFailureListener failureListener) {
        validateIds(eventId, entrantId);

        Task<Void> task = firestore.collection(COLLECTION_WAITING_LISTS)
                .document(eventId)
                .collection(SUB_COLLECTION_ENTRANTS)
                .document(entrantId)
                .update("status", STATUS_ACCEPTED);

        attachListeners(task, successListener, failureListener);
    }

    /**
     * Checks whether an entrant already joined the waiting list.
     *
     * @param eventId         event identifier
     * @param entrantId       entrant identifier
     * @param successListener invoked with the mapped entry (may be {@code null})
     * @param failureListener invoked when the read fails
     */
    public void fetchWaitingListEntry(@NonNull String eventId,
                                      @NonNull String entrantId,
                                      @Nullable OnSuccessListener<WaitingListEntry> successListener,
                                      @Nullable OnFailureListener failureListener) {
        firestore.collection(COLLECTION_WAITING_LISTS)
                .document(eventId)
                .collection(SUB_COLLECTION_ENTRANTS)
                .document(entrantId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (successListener != null) {
                        successListener.onSuccess(mapSnapshot(snapshot));
                    }
                })
                .addOnFailureListener(error -> {
                    if (failureListener != null) {
                        failureListener.onFailure(error);
                    }
                });
    }

    /**
     * Converts a document snapshot into a {@link WaitingListEntry}.
     *
     * @param snapshot Firestore document snapshot
     * @return mapped entry or {@code null} if not found
     */
    WaitingListEntry mapSnapshot(DocumentSnapshot snapshot) {
        if (snapshot == null || !snapshot.exists()) {
            return null;
        }
        return WaitingListEntry.fromSnapshot(snapshot);
    }

    /**
     * Builds the Firestore payload for a waiting-list entry.
     *
     * @param eventId   associated event id
     * @param eventName associated event name
     * @param entrantId entrant id stored inside the subcollection
     * @return map ready to persist
     */
    Map<String, Object> buildWaitingListData(String eventId,
                                             String eventName,
                                             String entrantId) {
        Map<String, Object> data = new HashMap<>();
        data.put("eventId", eventId);
        data.put("eventName", eventName);
        data.put("entrantId", entrantId);
        data.put("status", STATUS_PENDING);
        data.put("timestamp", FieldValue.serverTimestamp());
        return data;
    }

    /**
     * Ensures ids are non-null and non-empty.
     */
    private void validateIds(String eventId, String entrantId) {
        if (isNullOrBlank(eventId) || isNullOrBlank(entrantId)) {
            throw new IllegalArgumentException("Event id and entrant id are required");
        }
    }

    /**
     * Applies optional success/failure callbacks to Firestore tasks.
     */
    private void attachListeners(Task<Void> task,
                                 @Nullable OnSuccessListener<Void> successListener,
                                 @Nullable OnFailureListener failureListener) {
        if (successListener != null) {
            task.addOnSuccessListener(successListener);
        }
        if (failureListener != null) {
            task.addOnFailureListener(failureListener);
        }
    }

    /**
     * Simple helper for string validation.
     */
    private boolean isNullOrBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * Finds and promotes the next pending entrant for a given event.
     *
     * @param eventId          event identifier
     * @param successListener  forwarded when no replacement exists or promotion succeeds
     * @param failureListener  invoked when Firestore operations fail
     */
    private void promoteNextEntrant(@NonNull String eventId,
                                    @Nullable OnSuccessListener<Void> successListener,
                                    @Nullable OnFailureListener failureListener) {
        firestore.collection(COLLECTION_WAITING_LISTS)
                .document(eventId)
                .collection(SUB_COLLECTION_ENTRANTS)
                .whereEqualTo("status", STATUS_PENDING)
                .get()
                .addOnSuccessListener(snapshot -> {
                    DocumentSnapshot next = selectNextPending(snapshot);
                    if (next == null) {
                        if (successListener != null) successListener.onSuccess(null);
                        return;
                    }
                    next.getReference()
                            .update("status", STATUS_SELECTED)
                            .addOnSuccessListener(v -> {
                                if (successListener != null) successListener.onSuccess(null);
                            })
                            .addOnFailureListener(error -> {
                                if (failureListener != null) failureListener.onFailure(error);
                            });
                })
                .addOnFailureListener(error -> {
                    if (failureListener != null) failureListener.onFailure(error);
                });
    }

    /**
     * Determines which pending entrant should receive the next selection slot using timestamp order.
     *
     * @param snapshot Firestore query result containing pending entrants (may be {@code null})
     * @return Document snapshot representing the earliest entrant, or {@code null} when none exist
     */
    DocumentSnapshot selectNextPending(@Nullable QuerySnapshot snapshot) {
        if (snapshot == null) {
            return null;
        }
        DocumentSnapshot candidate = null;
        Date candidateTimestamp = null;
        for (DocumentSnapshot document : snapshot.getDocuments()) {
            com.google.firebase.Timestamp ts = document.getTimestamp("timestamp");
            Date timestamp = ts != null ? ts.toDate() : null;
            if (candidate == null) {
                candidate = document;
                candidateTimestamp = timestamp;
                continue;
            }
            if (candidateTimestamp == null) {
                if (timestamp != null) {
                    candidate = document;
                    candidateTimestamp = timestamp;
                }
                continue;
            }
            if (timestamp != null && timestamp.before(candidateTimestamp)) {
                candidate = document;
                candidateTimestamp = timestamp;
            }
        }
        return candidate;
    }
}
