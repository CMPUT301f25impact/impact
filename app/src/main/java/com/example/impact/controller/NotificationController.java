package com.example.impact.controller;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.impact.model.Admin;
import com.example.impact.model.Entrant;
import com.example.impact.model.EntrantHistoryItem;
import com.example.impact.model.Event;
import com.example.impact.model.Organizer;
import com.example.impact.model.User;
import com.example.impact.utils.FirebaseUtils;
import com.example.impact.utils.AppSession;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.impact.model.Notification;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.firebase.firestore.FieldValue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotificationController {
    private static final String COLLECTION_NOTIFICATIONS = "notifications";
    private final FirebaseFirestore firestore;

    private final UserController userController = new UserController();

    /**
     * Builds a controller using the shared Firestore instance.
     */
    public NotificationController() {
        this(AppSession.db());
    }

    /**
     * Builds a controller with an injected Firestore instance to ease testing.
     *
     * @param firestore Firestore reference, must not be {@code null}
     */
    public NotificationController(@NonNull FirebaseFirestore firestore) {
        this.firestore = firestore;
    }

    public com.google.android.gms.tasks.Task<com.google.firebase.firestore.QuerySnapshot>
    getNotificationsForEntrant(String entrantId) {
        return firestore.collection("notifications")
                .whereEqualTo("entrantId", entrantId)
                .get();
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
                .document(notification.getId())
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
            task.addOnSuccessListener(snapshot -> mapSnapshotToNotification(snapshot).addOnSuccessListener(notification -> {
                successListener.onSuccess(notification);
            }));
        }
        if (failureListener != null) {
            task.addOnFailureListener(failureListener);
        }
    }

    /**
     * Fetches all notifications for organizer reading.
     *
     * @param senders organizer users to query
     * @param successListener invoked with the mapped entrants list (never {@code null})
     * @param failureListener invoked if the read fails
     */
    public void fetchAllNotifications(
            User sender,
            @Nullable OnSuccessListener<List<Notification>> successListener,
            @Nullable OnFailureListener failureListener) {

        FirebaseFirestore db = FirebaseUtils.getFirestore();
        DocumentReference userDocRef = db.collection("users").document(sender.getId());

        db.collection("notifications")
                .whereEqualTo("sender", userDocRef)
                .get()
                .addOnSuccessListener(snapshot -> {
                    mapNotifications(snapshot, successListener, failureListener);
                })
                .addOnFailureListener(failureListener);
    }

    /**
     * Converts a snapshot into notification models
     *
     * @param snapshot Firestore query result
     * @return list of Notification models (never {@code null})
     */
    private void mapNotifications(
            @Nullable QuerySnapshot snapshot,
            @Nullable OnSuccessListener<List<Notification>> successListener,
            @Nullable OnFailureListener failureListener) {

        if (snapshot == null || snapshot.isEmpty()) {
            if (successListener != null) {
                successListener.onSuccess(new ArrayList<>());
            }
            return;
        }

        List<Task<Notification>> tasks = new ArrayList<>();
        for (DocumentSnapshot document : snapshot.getDocuments()) {
            tasks.add(mapSnapshotToNotification(document));
        }

        Tasks.whenAllSuccess(tasks).addOnSuccessListener(results -> {
            List<Notification> notifications = new ArrayList<>();
            for (Object result : results) {
                if (result != null) {
                    notifications.add((Notification) result);
                }
            }
            if (successListener != null) {
                successListener.onSuccess(notifications);
            }
        }).addOnFailureListener(e -> {
            if (failureListener != null) {
                failureListener.onFailure(e);
            }
        });
    }


    /**
     * Safely maps a snapshot into an {@link Notification}.
     *
     * @param snapshot Firestore document snapshot
     * @return entrant instance or {@code null} when snapshot missing
     */
    static Task<Notification> mapSnapshotToNotification(@Nullable DocumentSnapshot snapshot) {
        TaskCompletionSource<Notification> taskSource = new TaskCompletionSource<>();

        if (snapshot == null || !snapshot.exists()) {
            taskSource.setResult(null);
            return taskSource.getTask();
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                DocumentReference senderRef = snapshot.getDocumentReference("sender");
                DocumentReference eventRef = snapshot.getDocumentReference("related_event");
                List<DocumentReference> recipientsRefs = (List<DocumentReference>) snapshot.get("recipients");
                String message = snapshot.getString("message");
                Timestamp timestamp = snapshot.getTimestamp("time_stamp");
                Date timeStamp = timestamp != null ? timestamp.toDate() : null;
                String id = snapshot.getId();

                Organizer sender = Tasks.await(senderRef.get()).toObject(Organizer.class);
                Event related_event = Tasks.await(eventRef.get()).toObject(Event.class);

                ArrayList<User> recipients = new ArrayList<>();
                for (DocumentReference ref : recipientsRefs) {
                    recipients.add(Tasks.await(ref.get()).toObject(Entrant.class));
                }

                Notification notification = new Notification(id, sender, recipients, related_event, message, timeStamp);
                taskSource.setResult(notification);

            } catch (Exception e) {
                taskSource.setException(e);
            }
        });

        return taskSource.getTask();
    }

    public void createOfferNotification(String entrantId, String eventId, String eventName) {

        String notificationId = firestore.collection("notifications").document().getId();

        Map<String, Object> data = new HashMap<>();
        data.put("notificationId", notificationId);
        data.put("entrantId", entrantId);
        data.put("eventId", eventId);
        data.put("eventName", eventName);
        data.put("type", "offer");
        data.put("message",
                "You have been offered a spot in " + eventName +
                        ". To accept or decline, please visit " + eventName + " in your events list.");
        data.put("createdAt", FieldValue.serverTimestamp());
        data.put("read", false);

        firestore.collection("notifications")
                .document(notificationId)
                .set(data);
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
