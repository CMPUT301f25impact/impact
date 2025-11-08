package com.example.impact.controller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.impact.model.Admin;
import com.example.impact.model.Entrant;
import com.example.impact.model.EntrantHistoryItem;
import com.example.impact.model.Organizer;
import com.example.impact.model.User;
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
 * Coordinates Firestore persistence for user profile information.
 */
public class UserController {
    private static final String COLLECTION_USERS = "users";
    private static final String COLLECTION_GROUP_WAITING_LIST_ENTRANTS = "entrants";

    private final FirebaseFirestore firestore;

    /**
     * Builds a controller using the shared Firestore instance.
     */
    public UserController() {
        this(FirebaseUtil.getFirestore());
    }

    /**
     * Builds a controller with an injected Firestore instance to ease testing.
     *
     * @param firestore Firestore reference, must not be {@code null}
     */
    public UserController(@NonNull FirebaseFirestore firestore) {
        this.firestore = firestore;
    }

    /**
     * Persists the provided user profile to Firestore.
     *
     * @param user         user profile to save
     * @param successListener optional success callback
     * @param failureListener optional failure callback
     * @throws IllegalArgumentException when required entrant fields are missing
     */
    public void saveProfileToFirestore(@NonNull User user,
                                       @Nullable OnSuccessListener<Void> successListener,
                                       @Nullable OnFailureListener failureListener) {
        validateUser(user);
        Map<String, Object> data = buildUserData(user);

        Task<Void> task = firestore.collection(COLLECTION_USERS)
                .document(user.getId())
                .set(data);
        attachListeners(task, successListener, failureListener);
    }

    /**
     * Updates an existing user profile in Firestore using merge semantics.
     *
     * @param user        updated user model
     * @param successListener optional success callback
     * @param failureListener optional failure callback
     * @throws IllegalArgumentException when required entrant fields are missing
     */
    public void updateProfile(@NonNull User user,
                              @Nullable OnSuccessListener<Void> successListener,
                              @Nullable OnFailureListener failureListener) {
        validateUser(user);
        Map<String, Object> data = buildUserData(user);

        Task<Void> task = firestore.collection(COLLECTION_USERS)
                .document(user.getId())
                .set(data, SetOptions.merge());
        attachListeners(task, successListener, failureListener);
    }

    /**
     * Fetches the user profile and forwards it to the provided callback.
     *
     * @param userId       Firestore document id
     * @param successListener invoked with the mapped entrant (may be {@code null})
     * @param failureListener invoked if the read fails
     */
    public void fetchProfile(@NonNull String userId,
                             @Nullable OnSuccessListener<User> successListener,
                             @Nullable OnFailureListener failureListener) {
        Task<DocumentSnapshot> task = firestore.collection(COLLECTION_USERS)
                .document(userId)
                .get();

        if (successListener != null) {
            task.addOnSuccessListener(snapshot -> successListener.onSuccess(mapSnapshotToUser(snapshot)));
        }
        if (failureListener != null) {
            task.addOnFailureListener(failureListener);
        }
    }

    /**
     * Removes the user profile document from Firestore.
     *
     * @param userId       Firestore document id
     * @param successListener optional success callback
     * @param failureListener optional failure callback
     */
    public void deleteProfile(@NonNull String userId,
                              @Nullable OnSuccessListener<Void> successListener,
                              @Nullable OnFailureListener failureListener) {
        Task<Void> task = firestore.collection(COLLECTION_USERS)
                .document(userId)
                .delete();
        attachListeners(task, successListener, failureListener);
    }

    /**
     * Fetches all user profiles for administrative use.
     *
     * @param roles roles to query
     * @param successListener invoked with the mapped entrants list (never {@code null})
     * @param failureListener invoked if the read fails
     */
    public void fetchAllUsers(
            List<String> roles,
            @Nullable OnSuccessListener<List<User>> successListener,
                                 @Nullable OnFailureListener failureListener) {
        Task<QuerySnapshot> task = firestore.collection(COLLECTION_USERS)
                .whereIn("role", roles)
                .get();

        if (successListener != null) {
            task.addOnSuccessListener(snapshot -> successListener.onSuccess(mapUsers(snapshot)));
        }
        if (failureListener != null) {
            task.addOnFailureListener(failureListener);
        }
    }

    /**
     * Retrieves the entrant's waiting list history sorted by timestamp descending.
     *
     * @param entrantId       id of the entrant whose history is needed
     * @param successListener invoked with the mapped history list
     * @param failureListener invoked if the query fails
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

    /**
     * Converts a snapshot into user models
     *
     * @param snapshot Firestore query result
     * @return list of User models (never {@code null})
     */
    private List<User> mapUsers(@Nullable QuerySnapshot snapshot) {
        List<User> users = new ArrayList<>();
        if (snapshot == null) {
            return users;
        }
        for (DocumentSnapshot document : snapshot.getDocuments()) {
            User user = mapSnapshotToUser(document);
            if (user != null) {
                users.add(user);
            }
        }
        return users;
    }

    /**
     * Converts waiting-list documents into entrant history items.
     *
     * @param snapshot waiting list query result
     * @return sorted history items
     */
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

    /**
     * Safely maps a snapshot into an {@link User}.
     *
     * @param snapshot Firestore document snapshot
     * @return entrant instance or {@code null} when snapshot missing
     */
    static User mapSnapshotToUser(@Nullable DocumentSnapshot snapshot) {
        if (snapshot == null || !snapshot.exists()) {
            return null;
        }

        String role = snapshot.getString("role");
        if (role == null) return null;

        // Return correct user object determined by role
        User user;
        switch(role) {
            case Entrant.ROLE_KEY:
                user = snapshot.toObject(Entrant.class);
                break;
            case Organizer.ROLE_KEY:
                user = snapshot.toObject(Organizer.class);
                break;
            case Admin.ROLE_KEY:
                user = snapshot.toObject(Admin.class);
                break;
            default:
                user = null;
        }

        if (user == null) {
            user = new Entrant();
        }
        user.setId(snapshot.getId());
        return user;
    }

    /**
     * Ensures required fields exist before write operations.
     *
     * @param user model to validate
     */
    static void validateUser(@NonNull User user) {
        if (isNullOrBlank(user.getId())) {
            throw new IllegalArgumentException("User id is required");
        }
        if (isNullOrBlank(user.getName())) {
            throw new IllegalArgumentException("User name is required");
        }
        if (isNullOrBlank(user.getEmail())) {
            throw new IllegalArgumentException("User email is required");
        }
        if (isNullOrBlank(user.getRole())) {
            throw new IllegalArgumentException("User role is required");
        }
    }

    /**
     * Builds the Firestore payload for a given user.
     *
     * @param user model to serialize
     * @return map of primitive data ready for Firestore
     */
    static Map<String, Object> buildUserData(@NonNull User user) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("name", user.getName());
        data.put("email", user.getEmail());
        data.put("role", user.getRole());

        String phone = user.getPhone();
        data.put("phone", !isNullOrBlank(phone) ? phone : null);
        return data;
    }

    private static boolean isNullOrBlank(@Nullable String value) {
        return value == null || value.trim().isEmpty();
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
