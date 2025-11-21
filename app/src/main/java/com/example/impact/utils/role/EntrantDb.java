package com.example.impact.utils.role;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.impact.model.Entrant;
import com.example.impact.model.Event;
import com.example.impact.model.Image;
import com.example.impact.model.User;
import com.example.impact.model.WaitingListEntry;
import com.example.impact.utils.AppSession;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;

/**
 * Collection of Firestore helpers that power all entrant-facing behavior.
 * <p>
 * Every method in this class reads or mutates the {@code events/{eventId}} subtree or the
 * top-level {@code users/} collection on behalf of the currently authenticated entrant.
 * Controllers and fragments should use these helpers instead of constructing Firestore paths.
 */
public final class EntrantDb {

    private static final FirebaseFirestore db = AppSession.db();

    private EntrantDb() {
        // utility
    }

    /**
     * Loads every event document in {@code events/}.
     *
     * @return task delivering the mapped {@link Event} list
     */
    public static Task<List<Event>> listAllEvents() {
        TaskCompletionSource<List<Event>> tcs = new TaskCompletionSource<>();
        db.collection("events")
                .get()
                .addOnSuccessListener(snapshot -> tcs.setResult(mapEvents(snapshot)))
                .addOnFailureListener(tcs::setException);
        return tcs.getTask();
    }

    /**
     * Loads events from {@code events/} applying optional tag and date filters.
     *
     * @param tags interests to match against the {@code tags} field
     * @param startDate inclusive lower bound for {@code startDate}
     * @param endDate inclusive upper bound for {@code startDate}
     * @return task containing the filtered events
     */
    public static Task<List<Event>> listFilteredEvents(List<String> tags,
                                                       Date startDate,
                                                       Date endDate) {
        TaskCompletionSource<List<Event>> tcs = new TaskCompletionSource<>();
        Query query = db.collection("events");
        if (startDate != null && endDate != null) {
            query = query
                    .whereGreaterThanOrEqualTo("startDate", startDate)
                    .whereLessThanOrEqualTo("startDate", endDate);
        } else if (startDate != null) {
            query = query.whereGreaterThanOrEqualTo("startDate", startDate);
        } else if (endDate != null) {
            query = query.whereLessThanOrEqualTo("startDate", endDate);
        }
        if (tags != null && !tags.isEmpty()) {
            query = query.whereArrayContainsAny("tags", tags);
        }
        query.get()
                .addOnSuccessListener(snapshot -> tcs.setResult(mapEvents(snapshot)))
                .addOnFailureListener(tcs::setException);
        return tcs.getTask();
    }

    /**
     * Retrieves an image document stored under {@code events/{eventId}/images/{imageId}}.
     *
     * @param eventId parent event identifier
     * @param imageId document id within the {@code images} subcollection
     * @return task resolving with the decoded {@link Image}
     */
    public static Task<Image> fetchEventImage(@NonNull String eventId, @NonNull String imageId) {
        TaskCompletionSource<Image> tcs = new TaskCompletionSource<>();
        db.collection("events")
                .document(eventId)
                .collection("images")
                .document(imageId)
                .get()
                .addOnSuccessListener(snapshot -> tcs.setResult(Image.fromSnapshot(snapshot)))
                .addOnFailureListener(tcs::setException);
        return tcs.getTask();
    }

    /**
     * Creates or replaces a profile in {@code users/{userId}} for a brand-new entrant.
     *
     * @param user model to persist
     * @return task that completes when the document is written
     */
    public static Task<Void> saveProfile(@NonNull User user) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();
        db.collection("users")
                .document(user.getId())
                .set(buildUserPayload(user))
                .addOnSuccessListener(unused -> tcs.setResult(null))
                .addOnFailureListener(tcs::setException);
        return tcs.getTask();
    }

    /**
     * Applies partial updates to {@code users/{userId}} for an existing entrant.
     *
     * @param user model containing the merged updates
     * @return task that completes on success
     */
    public static Task<Void> updateProfile(@NonNull User user) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();
        db.collection("users")
                .document(user.getId())
                .set(buildUserPayload(user), SetOptions.merge())
                .addOnSuccessListener(unused -> tcs.setResult(null))
                .addOnFailureListener(tcs::setException);
        return tcs.getTask();
    }

    /**
     * Retrieves and maps {@code users/{userId}} into the appropriate {@link User} subtype.
     *
     * @param userId profile identifier
     * @return task yielding the mapped {@link User}
     */
    public static Task<User> fetchProfile(@NonNull String userId) {
        TaskCompletionSource<User> tcs = new TaskCompletionSource<>();
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(snapshot -> tcs.setResult(com.example.impact.controller.UserController.mapSnapshotToUser(snapshot)))
                .addOnFailureListener(tcs::setException);
        return tcs.getTask();
    }

    /**
     * Removes {@code users/{userId}} from Firestore.
     *
     * @param userId identifier to delete
     * @return task that completes when deletion succeeds
     */
    public static Task<Void> deleteProfile(@NonNull String userId) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();
        db.collection("users")
                .document(userId)
                .delete()
                .addOnSuccessListener(unused -> tcs.setResult(null))
                .addOnFailureListener(tcs::setException);
        return tcs.getTask();
    }

    /**
     * Creates/overwrites {@code events/{eventId}/waitingList/{entrantId}} linking the entrant to the event.
     *
     * @param eventId event to join
     * @param entrantId entrant user id
     * @return task that completes when the entry is created
     */
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

    /**
     * Deletes {@code events/{eventId}/waitingList/{entrantId}} to leave the queue.
     *
     * @param eventId event being left
     * @param entrantId entrant user id
     * @return task that completes when the entry is deleted
     */
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

    /**
     * Loads a single waiting-list entry for the entrant/event pair.
     *
     * @param eventId event identifier
     * @param entrantId entrant identifier
     * @return task yielding the mapped {@link WaitingListEntry}
     */
    public static Task<WaitingListEntry> fetchWaitingListEntry(@NonNull String eventId,
                                                               @NonNull String entrantId) {
        TaskCompletionSource<WaitingListEntry> tcs = new TaskCompletionSource<>();
        db.collection("events")
                .document(eventId)
                .collection("waitingList")
                .document(entrantId)
                .get()
                .addOnSuccessListener(snapshot -> tcs.setResult(WaitingListEntry.fromSnapshot(snapshot)))
                .addOnFailureListener(tcs::setException);
        return tcs.getTask();
    }

    /**
     * Retrieves every waiting-list record across events for the entrant via {@code collectionGroup("waitingList")}.
     *
     * @param entrantId entrant identifier
     * @return task containing the waiting-list history
     */
    public static Task<List<WaitingListEntry>> getEventHistory(@NonNull String entrantId) {
        TaskCompletionSource<List<WaitingListEntry>> tcs = new TaskCompletionSource<>();
        db.collectionGroup("waitingList")
                .whereEqualTo("entrantId", entrantId)
                .get()
                .addOnSuccessListener(snapshot -> tcs.setResult(mapEntries(snapshot)))
                .addOnFailureListener(tcs::setException);
        return tcs.getTask();
    }

    /**
     * Loads {@code events/{eventId}} and maps it to an {@link Event}.
     *
     * @param eventId document identifier
     * @return task with the mapped event
     */
    public static Task<Event> fetchEventDetails(@NonNull String eventId) {
        TaskCompletionSource<Event> tcs = new TaskCompletionSource<>();
        db.collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(snapshot -> tcs.setResult(Event.fromSnapshot(snapshot)))
                .addOnFailureListener(tcs::setException);
        return tcs.getTask();
    }

    /**
     * Removes {@code deviceId} references from any user documents associated with the device
     * so that future launches require explicit login.
     *
     * @param deviceId Android device identifier
     * @return task completing after all matches are updated
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

    /**
     * Looks up a user document by the {@code email} field.
     *
     * @param email entrant email address
     * @return task resolving with the first matching {@link DocumentSnapshot} or {@code null}
     */
    public static Task<DocumentSnapshot> findUserByEmail(@NonNull String email) {
        TaskCompletionSource<DocumentSnapshot> tcs = new TaskCompletionSource<>();
        db.collection("users")
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .addOnSuccessListener(snapshot -> tcs.setResult(snapshot.isEmpty() ? null : snapshot.getDocuments().get(0)))
                .addOnFailureListener(tcs::setException);
        return tcs.getTask();
    }

    /**
     * Looks up a user document by the {@code deviceId} field.
     *
     * @param deviceId Android device identifier
     * @return task resolving with the first matching document or {@code null}
     */
    public static Task<DocumentSnapshot> findUserByDeviceId(@NonNull String deviceId) {
        TaskCompletionSource<DocumentSnapshot> tcs = new TaskCompletionSource<>();
        db.collection("users")
                .whereEqualTo("deviceId", deviceId)
                .limit(1)
                .get()
                .addOnSuccessListener(snapshot -> tcs.setResult(snapshot.isEmpty() ? null : snapshot.getDocuments().get(0)))
                .addOnFailureListener(tcs::setException);
        return tcs.getTask();
    }

    /**
     * Checks whether a user already exists with the provided email.
     *
     * @param email email address to test
     * @return task resolving with {@code true} when a document exists
     */
    public static Task<Boolean> emailExists(@NonNull String email) {
        TaskCompletionSource<Boolean> tcs = new TaskCompletionSource<>();
        db.collection("users")
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .addOnSuccessListener(snapshot -> tcs.setResult(!snapshot.isEmpty()))
                .addOnFailureListener(tcs::setException);
        return tcs.getTask();
    }

    /**
     * Creates a new entrant profile document with the supplied credentials/device binding.
     *
     * @param email email used for login
     * @param password plaintext password stored temporarily for the prototype
     * @param deviceId device identifier bound to the session
     * @return task resolving with the created document id
     */
    public static Task<String> registerEntrantAccount(@NonNull String email,
                                                      @NonNull String password,
                                                      @NonNull String deviceId) {
        TaskCompletionSource<String> tcs = new TaskCompletionSource<>();
        Map<String, Object> data = new HashMap<>();
        data.put("email", email);
        data.put("password", password);
        data.put("role", Entrant.ROLE_KEY);
        data.put("deviceId", deviceId);
        data.put("createdAt", FieldValue.serverTimestamp());

        db.collection("users")
                .add(data)
                .addOnSuccessListener(doc -> tcs.setResult(doc.getId()))
                .addOnFailureListener(tcs::setException);
        return tcs.getTask();
    }

    /**
     * Updates the {@code deviceId} field on {@code users/{userId}}.
     *
     * @param userId user document id
     * @param deviceId nullable device identifier (use {@code null} to clear the binding)
     * @return task that completes when the field is updated
     */
    public static Task<Void> updateDeviceBinding(@NonNull String userId, @Nullable String deviceId) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();
        db.collection("users")
                .document(userId)
                .update("deviceId", deviceId)
                .addOnSuccessListener(unused -> tcs.setResult(null))
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

    private static List<Event> mapEvents(QuerySnapshot snapshot) {
        List<Event> events = new ArrayList<>();
        if (snapshot == null) {
            return events;
        }
        for (DocumentSnapshot doc : snapshot.getDocuments()) {
            events.add(Event.fromSnapshot(doc));
        }
        return events;
    }

    private static Map<String, Object> buildUserPayload(User user) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("name", user.getName());
        data.put("email", user.getEmail());
        data.put("role", user.getRole());
        data.put("phone", user.getPhone());
        return data;
    }
}
