package com.example.impact.controller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.impact.model.WaitingListEntry;
import com.example.impact.utils.FirebaseUtil;
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
    private static final String COLLECTION_WAITING_LISTS = "waitingLists";
    private static final String SUB_COLLECTION_ENTRANTS = "entrants";

    private final FirebaseFirestore firestore;

    public WaitingListController() {
        this(FirebaseUtil.getFirestore());
    }

    public WaitingListController(@NonNull FirebaseFirestore firestore) {
        this.firestore = firestore;
    }

    /**
     * Adds the entrant to the waiting list for the specified event.
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

    WaitingListEntry mapSnapshot(DocumentSnapshot snapshot) {
        if (snapshot == null || !snapshot.exists()) {
            return null;
        }
        return WaitingListEntry.fromSnapshot(snapshot);
    }

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

    private void validateIds(String eventId, String entrantId) {
        if (isNullOrBlank(eventId) || isNullOrBlank(entrantId)) {
            throw new IllegalArgumentException("Event id and entrant id are required");
        }
    }

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

    private boolean isNullOrBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
