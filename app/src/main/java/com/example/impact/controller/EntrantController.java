package com.example.impact.controller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.impact.model.Entrant;
import com.example.impact.model.EntrantHistoryItem;
import com.example.impact.model.WaitingListEntry;
import com.example.impact.utils.FirebaseUtil;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Coordinates Firestore persistence for entrant profile information.
 */
public class EntrantController {
    private static final String COLLECTION_ENTRANTS = "entrants";
    private static final String COLLECTION_GROUP_WAITING_LIST_ENTRANTS = "entrants";

    private final FirebaseFirestore firestore;

    /**
     * Builds a controller using the shared Firestore instance.
     */
    public EntrantController() {
        this(FirebaseUtil.getFirestore());
    }

    /**
     * Builds a controller with an injected Firestore instance to ease testing.
     *
     * @param firestore Firestore reference, must not be {@code null}
     */
    public EntrantController(@NonNull FirebaseFirestore firestore) {
        this.firestore = firestore;
    }

    /**
     * Persists the provided entrant profile to Firestore.
     *
     * @param entrant         entrant profile to save
     * @param successListener optional success callback
     * @param failureListener optional failure callback
     */
    public void saveProfileToFirestore(@NonNull Entrant entrant,
                                       @Nullable OnSuccessListener<Void> successListener,
                                       @Nullable OnFailureListener failureListener) {
        validateEntrant(entrant);
        Map<String, Object> data = buildEntrantData(entrant);

        Task<Void> task = firestore.collection(COLLECTION_ENTRANTS)
                .document(entrant.getId())
                .set(data);
        attachListeners(task, successListener, failureListener);
    }

    /**
     * Updates an existing entrant profile in Firestore using merge semantics.
     */
    public void updateProfile(@NonNull Entrant entrant,
                              @Nullable OnSuccessListener<Void> successListener,
                              @Nullable OnFailureListener failureListener) {
        validateEntrant(entrant);
        Map<String, Object> data = buildEntrantData(entrant);

        Task<Void> task = firestore.collection(COLLECTION_ENTRANTS)
                .document(entrant.getId())
                .set(data, SetOptions.merge());
        attachListeners(task, successListener, failureListener);
    }

    /**
     * Fetches the entrant profile and forwards it to the provided callback.
     */
    public void fetchProfile(@NonNull String entrantId,
                             @Nullable OnSuccessListener<Entrant> successListener,
                             @Nullable OnFailureListener failureListener) {
        Task<DocumentSnapshot> task = firestore.collection(COLLECTION_ENTRANTS)
                .document(entrantId)
                .get();

        if (successListener != null) {
            task.addOnSuccessListener(snapshot -> successListener.onSuccess(mapSnapshotToEntrant(snapshot)));
        }
        if (failureListener != null) {
            task.addOnFailureListener(failureListener);
        }
    }

    /**
     * Removes the entrant profile document from Firestore.
     */
    public void deleteProfile(@NonNull String entrantId,
                              @Nullable OnSuccessListener<Void> successListener,
                              @Nullable OnFailureListener failureListener) {
        Task<Void> task = firestore.collection(COLLECTION_ENTRANTS)
                .document(entrantId)
                .delete();
        attachListeners(task, successListener, failureListener);
    }

    /**
     * Fetches all entrant profiles for administrative use.
     */
    public void fetchAllEntrants(@Nullable OnSuccessListener<List<Entrant>> successListener,
                                 @Nullable OnFailureListener failureListener) {
        Task<QuerySnapshot> task = firestore.collection(COLLECTION_ENTRANTS)
                .get();

        if (successListener != null) {
            task.addOnSuccessListener(snapshot -> successListener.onSuccess(mapEntrants(snapshot)));
        }
        if (failureListener != null) {
            task.addOnFailureListener(failureListener);
        }
    }

    /**
     * Retrieves the entrant's waiting list history sorted by timestamp descending.
     */
    public void getEntrantHistory(@NonNull String entrantId,
                                  @Nullable OnSuccessListener<List<EntrantHistoryItem>> successListener,
                                  @Nullable OnFailureListener failureListener) {
        Task<QuerySnapshot> task = firestore.collectionGroup(COLLECTION_GROUP_WAITING_LIST_ENTRANTS)
                .whereEqualTo("entrantId", entrantId)
                .get();

        if (successListener != null) {
            task.addOnSuccessListener(snapshot -> successListener.onSuccess(mapHistory(snapshot)));
        }
        if (failureListener != null) {
            task.addOnFailureListener(failureListener);
        }
    }

    private List<Entrant> mapEntrants(@Nullable QuerySnapshot snapshot) {
        List<Entrant> entrants = new ArrayList<>();
        if (snapshot == null) {
            return entrants;
        }
        for (DocumentSnapshot document : snapshot.getDocuments()) {
            Entrant entrant = mapSnapshotToEntrant(document);
            if (entrant != null) {
                entrants.add(entrant);
            }
        }
        return entrants;
    }

    List<EntrantHistoryItem> mapHistory(QuerySnapshot snapshot) {
        List<EntrantHistoryItem> history = new ArrayList<>();
        for (DocumentSnapshot document : snapshot.getDocuments()) {
            WaitingListEntry entry = WaitingListEntry.fromSnapshot(document);
            history.add(new EntrantHistoryItem(
                    entry.getEventId(),
                    entry.getEventName(),
                    entry.getStatus() != null ? entry.getStatus() : "pending",
                    entry.getTimestamp()
            ));
        }
        history.sort((first, second) -> {
            if (first.getTimestamp() == null && second.getTimestamp() == null) {
                return 0;
            }
            if (first.getTimestamp() == null) {
                return 1;
            }
            if (second.getTimestamp() == null) {
                return -1;
            }
            return second.getTimestamp().compareTo(first.getTimestamp());
        });
        return history;
    }

    static Entrant mapSnapshotToEntrant(@Nullable DocumentSnapshot snapshot) {
        if (snapshot == null || !snapshot.exists()) {
            return null;
        }
        Entrant entrant = snapshot.toObject(Entrant.class);
        if (entrant == null) {
            entrant = new Entrant();
        }
        entrant.setId(snapshot.getId());
        return entrant;
    }

    static void validateEntrant(@NonNull Entrant entrant) {
        if (isNullOrBlank(entrant.getId())) {
            throw new IllegalArgumentException("Entrant id is required");
        }
        if (isNullOrBlank(entrant.getName())) {
            throw new IllegalArgumentException("Entrant name is required");
        }
        if (isNullOrBlank(entrant.getEmail())) {
            throw new IllegalArgumentException("Entrant email is required");
        }
    }

    static Map<String, Object> buildEntrantData(@NonNull Entrant entrant) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", entrant.getId());
        data.put("name", entrant.getName());
        data.put("email", entrant.getEmail());

        String phone = entrant.getPhone();
        data.put("phone", !isNullOrBlank(phone) ? phone : null);
        return data;
    }

    private static boolean isNullOrBlank(@Nullable String value) {
        return value == null || value.trim().isEmpty();
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
}
