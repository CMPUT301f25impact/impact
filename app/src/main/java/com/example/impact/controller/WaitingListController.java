package com.example.impact.controller;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.impact.model.Event;
import com.example.impact.model.User;
import com.example.impact.model.WaitingListEntry;
import com.example.impact.utils.AppSession;
import com.example.impact.view.OrganizerNotificationsFragment;
import com.example.impact.view.WaitingListActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles operations for joining or leaving event waiting lists.
 */
public class WaitingListController {
    private static final String COLLECTION_WAITING_LISTS = "waitingLists";
    private static final String COLLECTION_EVENTS = "events";
    private static final String SUB_COLLECTION_ENTRANTS = "entrants";

    private static final String STATUS_PENDING = "pending";
    private static final String STATUS_SELECTED = "selected";
    private static final String STATUS_ACCEPTED = "accepted";
    private static final String STATUS_CANCELLED = "cancelled";
    private static final String FIELD_LOTTERY_DONE = "lottery_done";

    private final FirebaseFirestore firestore;
    private final NotificationController notificationController;


    /**
     * Creates a controller backed by the default Firestore instance.
     */
    public WaitingListController() {
        this(AppSession.db(), new NotificationController(AppSession.db()));
    }

    /**
     * Creates a controller backed by the supplied Firestore instance (notifications disabled).
     *
     * @param firestore Firestore dependency, usually mocked during tests.
     */
    public WaitingListController(@NonNull FirebaseFirestore firestore) {
        this(firestore, null); // test mode => no notifications
    }

    /**
     * Creates a controller with explicit Firestore and notification collaborators.
     *
     * @param firestore Firestore dependency used for all reads/writes.
     * @param notificationController optional notification controller for entrant messaging.
     */
    public WaitingListController(@NonNull FirebaseFirestore firestore,
                                 @Nullable NotificationController notificationController) {
        this.firestore = firestore;
        this.notificationController = notificationController; // null during tests
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
     * Records a decline for a selected entrant, marking their entry as {@code "cancelled"},
     * and promotes the next eligible entrant when possible.
     *
     * @param eventId         event identifier
     * @param entrantId       entrant identifier
     * @param successListener invoked once the replacement flow completes (even when no replacement exists)
     * @param failureListener invoked when either decline or replacement writes fail
     */
    public void declineSelection(@NonNull String eventId,
                                 @NonNull String entrantId,
                                 @Nullable OnSuccessListener<Void> successListener,
                                 @Nullable OnFailureListener failureListener) {
        validateIds(eventId, entrantId);

        DocumentReference entryRef = firestore.collection(COLLECTION_WAITING_LISTS)
                .document(eventId)
                .collection(SUB_COLLECTION_ENTRANTS)
                .document(entrantId);

        entryRef.update("status", STATUS_CANCELLED)
                .addOnSuccessListener(v -> promoteNextEntrant(eventId, successListener, failureListener))
                .addOnFailureListener(error -> {
                    if (failureListener != null) {
                        failureListener.onFailure(error);
                    }
                });
    }

    /**
     * Public helper that draws a single replacement entrant from the pending pool.
     * Used by organizers when they press the "Redraw" button.
     *
     * @param eventId         event identifier
     * @param successListener invoked when the write succeeds
     * @param failureListener invoked when the write fails
     */
    public void redrawNextEntrant(@NonNull String eventId,
                                  @Nullable OnSuccessListener<Void> successListener,
                                  @Nullable OnFailureListener failureListener) {
        promoteNextEntrant(eventId, successListener, failureListener);
    }


    /**
     * Marks a selected entrant as having accepted their invitation.
     *
     * @param eventId         event identifier
     * @param entrantId       entrant identifier
     * @param successListener invoked when the write succeeds
     * @param failureListener invoked when the write fails
     */
    public void acceptSelection(@NonNull String eventId,
                                @NonNull String entrantId,
                                @Nullable OnSuccessListener<Void> successListener,
                                @Nullable OnFailureListener failureListener) {
        validateIds(eventId, entrantId);

        Task<Void> task = firestore.collection(COLLECTION_WAITING_LISTS)
                .document(eventId)
                .collection(SUB_COLLECTION_ENTRANTS)
                .document(entrantId)
                .update("status", STATUS_ACCEPTED);

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
     * Retrieves the number of entrants currently on the waiting list for the event.
     *
     * @param eventId         event identifier
     * @param successListener invoked with the count (never {@code null})
     * @param failureListener invoked when the read fails
     */
    public void fetchWaitingListCount(@NonNull String eventId,
                                      @Nullable OnSuccessListener<Integer> successListener,
                                      @Nullable OnFailureListener failureListener) {
        firestore.collection(COLLECTION_WAITING_LISTS)
                .document(eventId)
                .collection(SUB_COLLECTION_ENTRANTS)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (successListener != null) {
                        successListener.onSuccess(snapshot != null ? snapshot.size() : 0);
                    }
                })
                .addOnFailureListener(error -> {
                    if (failureListener != null) {
                        failureListener.onFailure(error);
                    }
                });
    }

    /**
     * Retrieves all waiting-list entries for a specific event.
     *
     * @param eventId         event identifier
     * @param successListener invoked with the mapped entry list
     * @param failureListener invoked when the read fails
     */
    public void fetchWaitingListByEventId(@NonNull String eventId,
                                      @Nullable OnSuccessListener<List<WaitingListEntry>> successListener,
                                      @Nullable OnFailureListener failureListener) {
        firestore.collection(COLLECTION_WAITING_LISTS)
                .document(eventId)
                .collection(SUB_COLLECTION_ENTRANTS)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<WaitingListEntry> waitingList = mapWaitingList(snapshot);
                    if (successListener != null) {
                        successListener.onSuccess(waitingList);
                    }
                })
                .addOnFailureListener(error -> {
                    if (failureListener != null) {
                        failureListener.onFailure(error);
                    }
                });
    }
    /**
     * Randomly selects pending entrants and marks them as selected.
     *
     * @param eventId         event identifier
     * @param limit           optional maximum entrants to select (selects all pending when {@code null})
     * @param successListener invoked once updates commit or when nothing needs updating
     * @param failureListener invoked when either the read or write fails
     */
    public void runLottery(@NonNull String eventId,
                           @Nullable Integer limit,
                           @Nullable OnSuccessListener<Void> successListener,
                           @Nullable OnFailureListener failureListener) {
        if (isNullOrBlank(eventId)) {
            throw new IllegalArgumentException("Event id is required");
        }

        firestore.collection(COLLECTION_WAITING_LISTS)
                .document(eventId)
                .collection(SUB_COLLECTION_ENTRANTS)
                .whereEqualTo("status", STATUS_PENDING)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<DocumentSnapshot> pending = snapshot != null
                            ? new ArrayList<>(snapshot.getDocuments())
                            : new ArrayList<>();
                    int selectionCount = determineSelectionCount(limit, pending.size());
                    if (selectionCount == 0) {
                        if (successListener != null) {
                            successListener.onSuccess(null);
                        }
                        return;
                    }
                    Collections.shuffle(pending);
                    WriteBatch batch = firestore.batch();
                    NotificationController notificationController = new NotificationController(firestore);
                    final String[] eventNameHolder = new String[1];

                    for (int i = 0; i < selectionCount; i++) {
                        DocumentSnapshot document = pending.get(i);
                        batch.update(document.getReference(), "status", STATUS_SELECTED);
                        if (i == 0 ) {
                            eventNameHolder[0] = document.getString("eventName");
                        }
                    }

                    Task<Void> commit = batch.commit();
                    commit.addOnSuccessListener(v -> {
                        updateLotteryState(eventId, true);
                        sendNotifications(eventId, eventNameHolder[0]);
                        if (successListener != null) {
                            successListener.onSuccess(v);
                        }
                    });
                    if (failureListener != null) {
                        commit.addOnFailureListener(failureListener);
                    }

                })
                .addOnFailureListener(error -> {
                    if (failureListener != null) {
                        failureListener.onFailure(error);
                    }
                });
    }
    public void sendNotifications(String eventId, String eventName) {
        NotificationController notificationController = new NotificationController(firestore);
        UserController userController = new UserController(firestore);

        fetchWaitingListByEventId(eventId, waitingList -> {
            List<Pair<User, WaitingListEntry>> globalWaitingListPairs = new ArrayList<>();
            List<User> globalSelectedUsers = new ArrayList<>();
            List<User> globalNotSelectedUsers = new ArrayList<>();

            List<String> queryOnRoles = Arrays.asList("entrant");
            userController.fetchAllUsers(queryOnRoles, userList -> {
                        Map<String, User> mapUsers = new HashMap<>();
                        for (User user : userList) {
                            mapUsers.put(user.getId(), user);
                        }
                        for (WaitingListEntry entry : waitingList) {
                            User user = mapUsers.get(entry.getEntrantId());
                            if (user != null) {
                                globalWaitingListPairs.add(new Pair<User, WaitingListEntry>(user, entry));
                            }
                        }
                        for (Pair<User, WaitingListEntry> pair : globalWaitingListPairs) {
                            if (pair.getEntry().getStatus().equals("selected")) {
                                globalSelectedUsers.add(pair.getUser());
                            }
                            else if (pair.getEntry().getStatus().equals("accepted")) {
                                globalSelectedUsers.add(pair.getUser());
                            }
                            else if (pair.getEntry().getStatus().equals("cancelled")) {
                                continue;
                            }
                            else {
                                globalNotSelectedUsers.add(pair.getUser()); // Get the not selected users
                            }
                        }
                        DocumentReference senderRef = AppSession.db().collection("users").document("system-organizer");
                        DocumentReference eventRef = AppSession.db().collection("events").document(eventId);
                        List<DocumentReference> selectedRecipientRefs = new ArrayList<>();
                        List<DocumentReference> notSelectedRecipientRefs = new ArrayList<>();

                        Map<String, Object> dataSelected = new HashMap<>();
                        dataSelected.put("sender", senderRef);
                        dataSelected.put("related_event", eventRef);
                        dataSelected.put("time_stamp", FieldValue.serverTimestamp());
                        if (!globalSelectedUsers.isEmpty()) {
                            for (User recipient : globalSelectedUsers) {
                                selectedRecipientRefs.add(AppSession.db().collection("users").document(recipient.getId()));
                            }
                            dataSelected.put("recipients", selectedRecipientRefs);
                            dataSelected.put("message", "You have been selected for event " + eventName);
                            AppSession.db().collection("notifications")
                                    .add(dataSelected);
                        }
                        Map<String, Object> data = new HashMap<>();
                        data.put("sender", senderRef);
                        data.put("related_event", eventRef);
                        data.put("time_stamp", FieldValue.serverTimestamp());
                        if (!globalNotSelectedUsers.isEmpty()) {
                            for (User recipient : globalNotSelectedUsers) {
                                notSelectedRecipientRefs.add(AppSession.db().collection("users").document(recipient.getId()));
                            }
                            data.put("recipients", notSelectedRecipientRefs);
                            data.put("message", "You have not been selected for event " + eventName + ". If someone cancels, you could still be selected and notified!");
                            AppSession.db().collection("notifications")
                                    .add(data);
                        }
                    },
                    exception -> {
                        Log.e("Notification", "Error creating notification: " + exception.getMessage());
                    });
        },
                error -> {
                    Log.e("Notification", "Error creating notification: " + error.getMessage());
                });

    }

    /**
     * Indicates whether the event already has selected entrants, implying the lottery was run.
     *
     * @param eventId         event identifier
     * @param successListener invoked with {@code true} when any selection exists
     * @param failureListener invoked when the read fails
     */
    public void hasLotteryRun(@NonNull String eventId,
                              @Nullable OnSuccessListener<Boolean> successListener,
                              @Nullable OnFailureListener failureListener) {
        if (isNullOrBlank(eventId)) {
            throw new IllegalArgumentException("Event id is required");
        }

        List<String> selectionStatuses = Arrays.asList(
                STATUS_SELECTED,
                STATUS_ACCEPTED,
                STATUS_CANCELLED
        );

        firestore.collection(COLLECTION_WAITING_LISTS)
                .document(eventId)
                .collection(SUB_COLLECTION_ENTRANTS)
                .whereIn("status", selectionStatuses)
                .limit(1)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (successListener != null) {
                        successListener.onSuccess(snapshot != null && !snapshot.isEmpty());
                    }
                })
                .addOnFailureListener(error -> {
                    if (failureListener != null) {
                        failureListener.onFailure(error);
                    }
                });
    }

    /**
     * Calculates how many entrants should be promoted based on pending size and limit.
     */
    private int determineSelectionCount(@Nullable Integer limit, int pendingSize) {
        if (pendingSize <= 0) {
            return 0;
        }
        if (limit == null) {
            return pendingSize;
        }
        int normalizedLimit = Math.max(limit, 0);
        return Math.min(normalizedLimit, pendingSize);
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
     * Maps a Firestore query into waiting-list entries for display.
     *
     * @param snapshot query result (may be {@code null})
     * @return mapped waiting list entry list
     */
    public List<WaitingListEntry> mapWaitingList(@Nullable QuerySnapshot snapshot) {
        List<WaitingListEntry> waitingList = new ArrayList<>();
        if (snapshot == null) {
            return waitingList;
        }
        snapshot.getDocuments().forEach(document -> waitingList.add(WaitingListEntry.fromSnapshot(document)));
        return waitingList;
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
        data.put("status", STATUS_PENDING);
        data.put("timestamp", FieldValue.serverTimestamp());
        return data;
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

    /**
     * Finds and redraws the next entrant randomly for a given event, issuing notifications to any
     * entrant promoted into the selected state.
     *
     * @param eventId          event identifier
     * @param successListener  forwarded when no replacement exists or promotion succeeds
     * @param failureListener  invoked when Firestore operations fail
     */
    private void promoteNextEntrant(@NonNull String eventId,
                                    @Nullable OnSuccessListener<Void> successListener,
                                    @Nullable OnFailureListener failureListener) {
        firestore.collection(COLLECTION_WAITING_LISTS)
                .document(eventId)
                .collection(SUB_COLLECTION_ENTRANTS)
                .whereEqualTo("status", STATUS_PENDING)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<DocumentSnapshot> pending = new ArrayList<>(snapshot.getDocuments());
                    if (pending.isEmpty()) {
                        if (successListener != null) successListener.onSuccess(null);
                        return;
                    }

                    Collections.shuffle(pending);
                    DocumentSnapshot next = pending.get(0);

                    next.getReference()
                            .update("status", STATUS_SELECTED)
                            .addOnSuccessListener(v -> {
                                if (notificationController != null) {
                                    String entrantId = next.getString("entrantId");
                                    String eventName = next.getString("eventName");
                                    if (entrantId != null && eventName != null) {
                                        notificationController.createOfferNotification(entrantId, eventId, eventName);
                                    }
                                }
                                if (successListener != null) successListener.onSuccess(null);
                            })
                            .addOnFailureListener(error -> {
                                if (failureListener != null) failureListener.onFailure(error);
                            });
                })
                .addOnFailureListener(error -> {
                    if (failureListener != null) failureListener.onFailure(error);
                });
    }

    /**
     * Determines which pending entrant should receive the next selection slot using timestamp order.
     *
     * @param snapshot Firestore query result containing pending entrants (may be {@code null})
     * @return Document snapshot representing the earliest entrant, or {@code null} when none exist
     */
    DocumentSnapshot selectNextPending(@Nullable QuerySnapshot snapshot) {
        if (snapshot == null) {
            return null;
        }
        DocumentSnapshot candidate = null;
        Date candidateTimestamp = null;
        for (DocumentSnapshot document : snapshot.getDocuments()) {
            com.google.firebase.Timestamp ts = document.getTimestamp("timestamp");
            Date timestamp = ts != null ? ts.toDate() : null;
            if (candidate == null) {
                candidate = document;
                candidateTimestamp = timestamp;
                continue;
            }
            if (candidateTimestamp == null) {
                if (timestamp != null) {
                    candidate = document;
                    candidateTimestamp = timestamp;
                }
                continue;
            }
            if (timestamp != null && timestamp.before(candidateTimestamp)) {
                candidate = document;
                candidateTimestamp = timestamp;
            }
        }
        return candidate;
    }

    /**
     * Marks the related event document with the latest lottery state.
     */
    private void updateLotteryState(@NonNull String eventId, boolean isDone) {
        firestore.collection(COLLECTION_EVENTS)
                .document(eventId)
                .update(FIELD_LOTTERY_DONE, isDone);
    }
    private static class Pair<User, WaitingListEntry> {
        private final User user;
        private final WaitingListEntry entry;

        public Pair(User user, WaitingListEntry entry) {
            this.user = user;
            this.entry = entry;
        }
        public User getUser() {return user;}
        public WaitingListEntry getEntry() {return entry;}
    }
}
