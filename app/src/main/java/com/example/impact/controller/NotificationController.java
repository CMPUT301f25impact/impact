package com.example.impact.controller;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.impact.model.Admin;
import com.example.impact.model.Entrant;
import com.example.impact.model.EntrantHistoryItem;
import com.example.impact.model.Event;
import com.example.impact.model.Organizer;
import com.example.impact.model.User;
import com.example.impact.utils.FirebaseUtil;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.impact.model.Notification;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationController {
    private static final String COLLECTION_NOTIFICATIONS = "notifications";
    private final FirebaseFirestore firestore;

    /**
     * Builds a controller using the shared Firestore instance.
     */
    public NotificationController() {
        this(FirebaseUtil.getFirestore());
    }

    /**
     * Builds a controller with an injected Firestore instance to ease testing.
     *
     * @param firestore Firestore reference, must not be {@code null}
     */
    public NotificationController(@NonNull FirebaseFirestore firestore) {
        this.firestore = firestore;
    }

    /**
     * Persists the provided notification to Firestore.
     *
     * @param notification         notification to save
     * @param successListener optional success callback
     * @param failureListener optional failure callback
     * @throws IllegalArgumentException when required entrant fields are missing
     */
    public void saveNotificationToFirestore(@NonNull Notification notification,
                                       @Nullable OnSuccessListener<Void> successListener,
                                       @Nullable OnFailureListener failureListener) {
        Map<String, Object> data = buildNotificationData(notification);

        Task<Void> task = firestore.collection(COLLECTION_NOTIFICATIONS)
                .document(notification.getPK())
                .set(data);
        attachListeners(task, successListener, failureListener);
    }

    public void fetchNotification(@NonNull String notificationId,
                                  @Nullable OnSuccessListener<Notification> successListener,
                                  @Nullable OnFailureListener failureListener) {
        Task<DocumentSnapshot> task = firestore.collection(COLLECTION_NOTIFICATIONS)
                .document(notificationId)
                .get();
        if (successListener != null) {
            task.addOnSuccessListener(snapshot -> successListener.onSuccess(mapSnapshotToNotification(snapshot)));
        }
        if (failureListener != null) {
            task.addOnFailureListener(failureListener);
        }
    }

    /**
     * Fetches all notifications for organizer reading.
     *
     * @param senders roles to query
     * @param successListener invoked with the mapped entrants list (never {@code null})
     * @param failureListener invoked if the read fails
     */
    public void fetchAllNotifications(
            List<User> senders,
            @Nullable OnSuccessListener<List<Notification>> successListener,
            @Nullable OnFailureListener failureListener) {
        Task<QuerySnapshot> task = firestore.collection(COLLECTION_NOTIFICATIONS)
                .whereIn("sender", senders)
                .get();

        if (successListener != null) {
            task.addOnSuccessListener(snapshot -> successListener.onSuccess(mapNotifications(snapshot)));
        }
        if (failureListener != null) {
            task.addOnFailureListener(failureListener);
        }
    }

    /**
     * Converts a snapshot into notification models
     *
     * @param snapshot Firestore query result
     * @return list of Notification models (never {@code null})
     */
    private List<Notification> mapNotifications(@Nullable QuerySnapshot snapshot) {
        List<Notification> notifications = new ArrayList<>();
        if (snapshot == null) {
            return notifications;
        }
        for (DocumentSnapshot document : snapshot.getDocuments()) {
            Notification notification = mapSnapshotToNotification(document);
            if (notification != null) {
                notifications.add(notification);
            }
        }
        return notifications;
    }

    /**
     * Safely maps a snapshot into an {@link User}.
     *
     * @param snapshot Firestore document snapshot
     * @return entrant instance or {@code null} when snapshot missing
     */
    static Notification mapSnapshotToNotification(@Nullable DocumentSnapshot snapshot) {
        if (snapshot == null || !snapshot.exists()) {
            return null;
        }

        String pk = snapshot.getString("pk");
        if (pk == null) return null;

        Notification notification;
        notification = snapshot.toObject(Notification.class);
        return notification;
    }

    /**
     * Builds the Firestore payload for a given notification.
     *
     * @param notification model to serialize
     * @return map of primitive data ready for Firestore
     */
    static Map<String, Object> buildNotificationData(@NonNull Notification notification) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", notification.getPK());
        data.put("sender", notification.getSender());
        data.put("recipients", notification.getRecipients());
        data.put("related_event", notification.getRelated_event());
        data.put("message", notification.getMessage());
        data.put("time_stamp", notification.getTime_stamp());
        return data;
    }

    /**
     * Applies optional success/failure listeners to a Firestore task.
     *
     * @param task             Firestore task to observe
     * @param successListener  optional success callback
     * @param failureListener  optional failure callback
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

}
