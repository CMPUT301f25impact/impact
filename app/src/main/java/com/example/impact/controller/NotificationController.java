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

/**
 * Coordinates creation, retrieval, and mapping of notification documents.
 */
public class NotificationController {
    private static final String COLLECTION_NOTIFICATIONS = "notifications";
    private final FirebaseFirestore firestore;

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

    /**
     * Retrieves notifications where the entrant appears in the recipient list.
     *
     * @param entrant          entrant whose notifications should be fetched
     * @param successListener  invoked with mapped notifications (never {@code null})
     * @param failureListener  invoked if the read fails
     */
    public void getNotificationsForEntrant(Entrant entrant,
                                           @Nullable OnSuccessListener<List<Notification>> successListener,
                                           @Nullable OnFailureListener failureListener) {
        FirebaseFirestore db = FirebaseUtils.getFirestore();
        DocumentReference entrantDocRef = db.collection("users").document(entrant.getId());
        db.collection("notifications")
                .whereArrayContains("recipients", entrantDocRef)
                .get()
                .addOnSuccessListener(snapshot -> {
                    mapNotifications(snapshot, successListener, failureListener);
                })
                .addOnFailureListener(failureListener);
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

    /**
     * Deletes the notification with the provided id.
     *
     * @param notificationId notification ID
     * @param successListener executed on success
     * @param failureListener executed on failure
     */
    public void deleteNotification(@NonNull String notificationId,
                            @Nullable OnSuccessListener<String> successListener,
                            @Nullable OnFailureListener failureListener) {
        firestore.collection(COLLECTION_NOTIFICATIONS)
                .document(notificationId)
                .delete()
                .addOnSuccessListener(v -> {
                    if (successListener != null) successListener.onSuccess(notificationId);
                })
                .addOnFailureListener(err -> {
                    if (failureListener != null) failureListener.onFailure(err);
                });
    }

    /**
     * Builds the Firestore payload for a given notification.
     *
     * @param notification model to serialize
     * @return map of primitive data ready for Firestore
     */
    static Map<String, Object> buildNotificationData(@NonNull Notification notification) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", notification.getId());
        data.put("sender", notification.getSender());
        data.put("recipients", notification.getRecipients());
        data.put("related_event", notification.getRelated_event()); // Does this exist?
        data.put("message", notification.getMessage());
        data.put("time_stamp", notification.getTime_stamp());
        return data;
    }

    /**
     * Retrieves a single notification by id.
     *
     * @param notificationId Firestore document id
     * @param successListener invoked with the mapped notification (may be {@code null})
     * @param failureListener invoked if the read fails
     */
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
     * Fetches all notifications sent by the provided organizer/user.
     *
     * @param sender user document whose sent notifications should be retrieved
     * @param successListener invoked with the mapped notifications list
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
     * Loads all available notifications without filtering.
     *
     * @param successListener invoked with the mapped notifications list
     * @param failureListener invoked when the Firestore read fails
     */
    public void fetchAvailableNotifications(@Nullable OnSuccessListener<List<Notification>> successListener,
                                     @Nullable OnFailureListener failureListener) {
        firestore.collection(COLLECTION_NOTIFICATIONS)
                .get()
                .addOnSuccessListener(snapshot -> {
                    mapNotifications(snapshot, successListener, failureListener);
                })
                .addOnFailureListener(failureListener);
    }



    /**
     * Converts a snapshot into notification models.
     *
     * @param snapshot Firestore query result
     * @param successListener optional success callback
     * @param failureListener optional failure callback
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
     * Asynchronously maps a snapshot into a {@link Notification}.
     *
     * @param snapshot Firestore document snapshot
     * @return task resolving to the mapped notification (or {@code null})
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

    /**
     * Creates a simple offer notification used when lottery selections promote an entrant.
     *
     * @param entrantId entrant receiving the offer
     * @param eventId   event identifier
     * @param eventName event display name used in the message
     */
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
