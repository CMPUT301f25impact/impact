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
 * Helpers for entrant-centric Firestore operations.
 */
public final class EntrantDb {

    private static final FirebaseFirestore db = AppSession.db();

    private EntrantDb() {
        // utility
    }

    public static Task<List<Event>> listAllEvents() {
        TaskCompletionSource<List<Event>> tcs = new TaskCompletionSource<>();
        db.collection("events")
                .get()
                .addOnSuccessListener(snapshot -> tcs.setResult(mapEvents(snapshot)))
                .addOnFailureListener(tcs::setException);
        return tcs.getTask();
    }

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

    public static Task<Void> saveProfile(@NonNull User user) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();
        db.collection("users")
                .document(user.getId())
                .set(buildUserPayload(user))
                .addOnSuccessListener(unused -> tcs.setResult(null))
                .addOnFailureListener(tcs::setException);
        return tcs.getTask();
    }

    public static Task<Void> updateProfile(@NonNull User user) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();
        db.collection("users")
                .document(user.getId())
                .set(buildUserPayload(user), SetOptions.merge())
                .addOnSuccessListener(unused -> tcs.setResult(null))
                .addOnFailureListener(tcs::setException);
        return tcs.getTask();
    }

    public static Task<User> fetchProfile(@NonNull String userId) {
        TaskCompletionSource<User> tcs = new TaskCompletionSource<>();
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(snapshot -> tcs.setResult(com.example.impact.controller.UserController.mapSnapshotToUser(snapshot)))
                .addOnFailureListener(tcs::setException);
        return tcs.getTask();
    }

    public static Task<Void> deleteProfile(@NonNull String userId) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();
        db.collection("users")
                .document(userId)
                .delete()
                .addOnSuccessListener(unused -> tcs.setResult(null))
                .addOnFailureListener(tcs::setException);
        return tcs.getTask();
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
