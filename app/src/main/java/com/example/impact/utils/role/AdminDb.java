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
 * Firestore helpers dedicated to administrator operations.
 */
public final class AdminDb {

    private static final FirebaseFirestore db = AppSession.db();

    private AdminDb() {
        // no-op
    }

    public static Task<List<Event>> listAllEvents() {
        TaskCompletionSource<List<Event>> tcs = new TaskCompletionSource<>();
        FirebaseUtils.getAllDocuments("events", Event.class,
                events -> tcs.setResult(events != null ? events : Collections.emptyList()),
                tcs::setException);
        return tcs.getTask();
    }

    public static Task<List<User>> listAllProfiles() {
        TaskCompletionSource<List<User>> tcs = new TaskCompletionSource<>();
        FirebaseUtils.getAllDocuments("users", User.class,
                users -> tcs.setResult(users != null ? users : Collections.emptyList()),
                tcs::setException);
        return tcs.getTask();
    }

    public static Task<Void> deleteEvent(@NonNull String eventId) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();
        FirebaseUtils.deleteDocument("events", eventId,
                unused -> tcs.setResult(null),
                tcs::setException);
        return tcs.getTask();
    }

    public static Task<Void> deleteUser(@NonNull String userId) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();
        FirebaseUtils.deleteDocument("users", userId,
                unused -> tcs.setResult(null),
                tcs::setException);
        return tcs.getTask();
    }

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
}
