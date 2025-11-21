package com.example.impact.controller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.impact.model.WaitingListEntry;
import com.example.impact.utils.role.EntrantDb;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Handles operations for joining or leaving event waiting lists.
 */
public class WaitingListController {

    public WaitingListController() { }

    public WaitingListController(@NonNull FirebaseFirestore unused) { }

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

        Task<Void> task = EntrantDb.joinWaitingList(eventId, entrantId);
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

        Task<Void> task = EntrantDb.leaveWaitingList(eventId, entrantId);
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
        Task<WaitingListEntry> task = EntrantDb.fetchWaitingListEntry(eventId, entrantId);
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
    private <T> void attachListeners(Task<T> task,
                                 @Nullable OnSuccessListener<T> successListener,
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
