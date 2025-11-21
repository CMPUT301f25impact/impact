package com.example.impact.utils.role;

import androidx.annotation.NonNull;

import com.example.impact.model.Event;
import com.example.impact.model.Image;
import com.example.impact.model.User;
import com.example.impact.utils.AppSession;
import com.example.impact.utils.FirebaseUtils;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Aggregates all administrator-facing Firestore operations.
 * <p>
 * All methods in this class target top-level collections (such as {@code events/} and {@code users/})
 * or traverse nested subcollections under {@code events/{eventId}} in order to gather global data
 * like event lists, profile lists, or event-scoped images. Controllers and activities should prefer
 * these helpers over building Firestore paths manually.
 */
public final class AdminDb {

    private static final FirebaseFirestore db = AppSession.db();

    private AdminDb() {
        // no-op
    }

    /**
     * Retrieves every event document in the {@code events/} collection.
     *
     * @return task that resolves with a list of {@link Event} models (never {@code null})
     */
    public static Task<List<Event>> listAllEvents() {
        TaskCompletionSource<List<Event>> tcs = new TaskCompletionSource<>();
        FirebaseUtils.getAllDocuments("events", Event.class,
                events -> tcs.setResult(events != null ? events : Collections.emptyList()),
                tcs::setException);
        return tcs.getTask();
    }

    /**
     * Retrieves every user profile stored in the {@code users/} collection.
     *
     * @return task that resolves with a list of {@link User} instances
     */
    public static Task<List<User>> listAllProfiles() {
        TaskCompletionSource<List<User>> tcs = new TaskCompletionSource<>();
        FirebaseUtils.getAllDocuments("users", User.class,
                users -> tcs.setResult(users != null ? users : Collections.emptyList()),
                tcs::setException);
        return tcs.getTask();
    }

    /**
     * Deletes an event document at {@code events/{eventId}}.
     *
     * @param eventId Firestore identifier of the event to remove
     * @return task that completes when the delete succeeds
     */
    public static Task<Void> deleteEvent(@NonNull String eventId) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();
        FirebaseUtils.deleteDocument("events", eventId,
                unused -> tcs.setResult(null),
                tcs::setException);
        return tcs.getTask();
    }

    /**
     * Deletes a user profile document at {@code users/{userId}}.
     *
     * @param userId Firestore identifier of the user to remove
     * @return task that completes when the delete succeeds
     */
    public static Task<Void> deleteUser(@NonNull String userId) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();
        FirebaseUtils.deleteDocument("users", userId,
                unused -> tcs.setResult(null),
                tcs::setException);
        return tcs.getTask();
    }

    /**
     * Retrieves every image stored under the nested {@code events/{eventId}/images/} subcollections.
     *
     * @return task that resolves with a flattened list of {@link Image} objects
     */
    public static Task<List<Image>> listAllImagesAcrossEvents() {
        TaskCompletionSource<List<Image>> tcs = new TaskCompletionSource<>();
        db.collection("events")
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.isEmpty()) {
                        tcs.setResult(Collections.emptyList());
                        return;
                    }
                    List<Image> aggregated = Collections.synchronizedList(new ArrayList<>());
                    AtomicInteger pending = new AtomicInteger(snapshot.size());
                    AtomicBoolean failed = new AtomicBoolean(false);
                    for (DocumentSnapshot event : snapshot.getDocuments()) {
                        event.getReference()
                                .collection("images")
                                .get()
                                .addOnSuccessListener(images -> {
                                    for (DocumentSnapshot doc : images.getDocuments()) {
                                        aggregated.add(Image.fromSnapshot(doc));
                                    }
                                    if (pending.decrementAndGet() == 0 && !failed.get()) {
                                        tcs.setResult(new ArrayList<>(aggregated));
                                    }
                                })
                                .addOnFailureListener(error -> {
                                    if (failed.compareAndSet(false, true)) {
                                        tcs.setException(error);
                                    }
                                });
                    }
                })
                .addOnFailureListener(tcs::setException);
        return tcs.getTask();
    }

    /**
     * Deletes a single image document under {@code events/{eventId}/images/{imageId}}.
     *
     * @param eventId event that owns the image
     * @param imageId specific image document identifier
     * @return task that completes once the image is removed
     */
    public static Task<Void> deleteEventImage(@NonNull String eventId, @NonNull String imageId) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();
        db.collection("events")
                .document(eventId)
                .collection("images")
                .document(imageId)
                .delete()
                .addOnSuccessListener(unused -> tcs.setResult(null))
                .addOnFailureListener(tcs::setException);
        return tcs.getTask();
    }

    /**
     * Clears the {@code deviceId} field on any user documents bound to the provided device.
     * Used by admin-driven logouts to ensure the next app launch prompts for credentials.
     *
     * @param deviceId Android device identifier to remove
     * @return task that completes after all matching documents have been updated
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
}
