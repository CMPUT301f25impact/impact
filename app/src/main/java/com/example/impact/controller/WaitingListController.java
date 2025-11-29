package com.example.impact.controller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.impact.model.WaitingListEntry;
import com.example.impact.utils.AppSession;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles operations for joining or leaving event waiting lists.
 */
public class WaitingListController {
    static final String COLLECTION_EVENTS = "events";
    private static final String COLLECTION_WAITING_LISTS = "waitingLists";
    private static final String SUB_COLLECTION_ENTRANTS = "entrants";
    /**
     * Error code used when the waiting list limit prevents joining.
     */
    public static final String ERROR_WAITING_LIST_LIMIT_REACHED = "waiting_list_limit_reached";

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
        firestore.collection(COLLECTION_EVENTS)
                .document(eventId)
                .get()
                .addOnSuccessListener(eventSnapshot -> {
                    Long limit = eventSnapshot != null ? eventSnapshot.getLong("maxEntrants") : null;
                    if (limit == null || limit <= 0) {
                        writeWaitingListEntry(eventId, eventName, entrantId, successListener, failureListener);
                        return;
                    }
                    final int limitValue = limit.intValue();

                    firestore.collection(COLLECTION_WAITING_LISTS)
                            .document(eventId)
                            .collection(SUB_COLLECTION_ENTRANTS)
                            .get()
                            .addOnSuccessListener(snapshot -> {
                                boolean alreadyJoined = false;
                                for (DocumentSnapshot entrantSnapshot : snapshot.getDocuments()) {
                                    if (entrantId.equals(entrantSnapshot.getId())) {
                                        alreadyJoined = true;
                                        break;
                                    }
                                }

                                if (alreadyJoined || snapshot.size() < limitValue) {
                                    writeWaitingListEntry(eventId, eventName, entrantId, successListener, failureListener);
                                } else if (failureListener != null) {
                                    failureListener.onFailure(new IllegalStateException(ERROR_WAITING_LIST_LIMIT_REACHED));
                                }
                            })
                            .addOnFailureListener(error -> {
                                if (failureListener != null) {
                                    failureListener.onFailure(error);
                                }
                            });
                })
                .addOnFailureListener(error -> {
                    if (failureListener != null) {
                        failureListener.onFailure(error);
                    }
                });
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
        data.put("status", "pending");
        data.put("timestamp", FieldValue.serverTimestamp());
        return data;
    }

    private void writeWaitingListEntry(@NonNull String eventId,
                                       @NonNull String eventName,
                                       @NonNull String entrantId,
                                       @Nullable OnSuccessListener<Void> successListener,
                                       @Nullable OnFailureListener failureListener) {
        Map<String, Object> data = buildWaitingListData(eventId, eventName, entrantId);
        Task<Void> task = firestore.collection(COLLECTION_WAITING_LISTS)
                .document(eventId)
                .collection(SUB_COLLECTION_ENTRANTS)
                .document(entrantId)
                .set(data);

        attachListeners(task, successListener, failureListener);
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
}
