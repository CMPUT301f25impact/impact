package com.example.impact.controller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.impact.model.Event;
import com.example.impact.utils.AppSession;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles event retrieval and filtering logic between Firestore and the UI.
 */
public class EventController {
    private static final String COLLECTION_EVENTS = "events";
    private static final String SUB_COLLECTION_REGISTRATIONS = "registeredEntrants";

    private final FirebaseFirestore firestore;

    /**
     * Builds a controller backed by the shared Firestore instance.
     */
    public EventController() {
        this(AppSession.db());
    }

    /**
     * Builds a controller with an injected Firestore instance (useful for tests).
     *
     * @param firestore Firestore dependency
     */
    public EventController(@NonNull FirebaseFirestore firestore) {
        this.firestore = firestore;
    }

    /**
     * Loads all available events.
     *
     * @param successListener invoked with the mapped events list
     * @param failureListener invoked when the Firestore read fails
     */
    public void fetchAvailableEvents(@Nullable OnSuccessListener<List<Event>> successListener,
                                     @Nullable OnFailureListener failureListener) {
        firestore.collection(COLLECTION_EVENTS)
                .get()
                .addOnSuccessListener(snapshot -> dispatchEvents(successListener, snapshot))
                .addOnFailureListener(error -> {
                    if (failureListener != null) {
                        failureListener.onFailure(error);
                    }
                });
    }

    /**
     * Performs a filtered fetch constrained by tags and date range.
     *
     * @param tags            list of interests to match against event tags
     * @param startDate       inclusive lower bound for event start date
     * @param endDate         inclusive upper bound for event start date
     * @param successListener invoked with the filtered events
     * @param failureListener invoked when the query fails
     */
    public void fetchFilteredEvents(@Nullable List<String> tags,
                                    @Nullable Date startDate,
                                    @Nullable Date endDate,
                                    @Nullable OnSuccessListener<List<Event>> successListener,
                                    @Nullable OnFailureListener failureListener) {
        Query query = firestore.collection(COLLECTION_EVENTS);

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
                .addOnSuccessListener(snapshot -> dispatchEvents(successListener, snapshot))
                .addOnFailureListener(error -> {
                    if (failureListener != null) {
                        failureListener.onFailure(error);
                    }
                });
    }

    /**
     * Fetches a single event by organizer.
     *
     * @param organizerId Firestore id of the organizer
     * @param success     invoked with the organizer's events
     * @param failure     invoked when the read fails
     */
    public void fetchEventsByOrganizer(@NonNull String organizerId,
                                       @Nullable OnSuccessListener<List<Event>> success,
                                       @Nullable OnFailureListener failure) {
        firestore.collection(COLLECTION_EVENTS)
                .whereEqualTo("organizerId", organizerId)
                .get()
                .addOnSuccessListener(snap -> {
                    List<Event> events = mapEvents(snap);
                    if (success != null) success.onSuccess(events);
                })
                .addOnFailureListener(e -> { if (failure != null) failure.onFailure(e); });
    }

    /**
     * Subscribes to real-time updates for events owned by an organizer email.
     *
     * @param email organizer email address
     * @param listener callback invoked with snapshot updates
     * @return listener registration to dispose of updates
     */
    public ListenerRegistration listenEventsByOrganizer(
            @NonNull String email,
            @NonNull EventListener<QuerySnapshot> listener) {

        return firestore.collection("events")
                .whereEqualTo("organizerEmail", email)
                .addSnapshotListener(listener);
    }


    /**
     * Creates an event document. The Event object should have:
     * name, description, startDate, endDate, (optional) capacity, organizerId.
     * Returns the new document id via successListener.
     *
     * @param event           event model to persist
     * @param successListener invoked with the generated document id
     * @param failureListener invoked when the write fails
     */
    public void createEvent(@NonNull Event event,
                            @Nullable OnSuccessListener<String> successListener,
                            @Nullable OnFailureListener failureListener) {
        firestore.collection(COLLECTION_EVENTS)
                .add(event)
                .addOnSuccessListener(ref -> {
                    if (successListener != null) successListener.onSuccess(ref.getId());
                })
                .addOnFailureListener(err -> {
                    if (failureListener != null) failureListener.onFailure(err);
                });
    }

    /**
     * Fetches an event document by id and forwards it to the provided callback.
     *
     * @param eventId          Firestore document id
     * @param successListener  invoked with the mapped event (may be {@code null} when missing)
     * @param failureListener  invoked when the read fails
     */
    public void fetchEventById(@NonNull String eventId,
                               @Nullable OnSuccessListener<Event> successListener,
                               @Nullable OnFailureListener failureListener) {
        firestore.collection(COLLECTION_EVENTS)
                .document(eventId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (successListener != null) {
                        Event event = (snapshot != null && snapshot.exists()) ? Event.fromSnapshot(snapshot) : null;
                        successListener.onSuccess(event);
                    }
                })
                .addOnFailureListener(error -> {
                    if (failureListener != null) {
                        failureListener.onFailure(error);
                    }
                });
    }

    /**
     * Registers an entrant for the target event by storing their id within the event registrations subcollection.
     *
     * @param eventId         id of the event document
     * @param entrantId       id of the entrant joining the event
     * @param successListener optional success callback
     * @param failureListener optional failure callback
     */
    public void registerEntrantForEvent(@NonNull String eventId,
                                        @NonNull String entrantId,
                                        @Nullable OnSuccessListener<Void> successListener,
                                        @Nullable OnFailureListener failureListener) {
        validateIds(eventId, entrantId);
        Map<String, Object> data = new HashMap<>();
        data.put("entrantId", entrantId);
        data.put("timestamp", FieldValue.serverTimestamp());

        Task<Void> task = firestore.collection(COLLECTION_EVENTS)
                .document(eventId)
                .collection(SUB_COLLECTION_REGISTRATIONS)
                .document(entrantId)
                .set(data);
        attachListeners(task, successListener, failureListener);
    }

    /**
     * Determines whether the entrant already registered for the given event.
     *
     * @param eventId         event identifier
     * @param entrantId       entrant identifier
     * @param successListener invoked with {@code true} when a registration document exists
     * @param failureListener invoked when the read fails
     */
    public void fetchRegistrationStatus(@NonNull String eventId,
                                        @NonNull String entrantId,
                                        @Nullable OnSuccessListener<Boolean> successListener,
                                        @Nullable OnFailureListener failureListener) {
        firestore.collection(COLLECTION_EVENTS)
                .document(eventId)
                .collection(SUB_COLLECTION_REGISTRATIONS)
                .document(entrantId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (successListener != null) {
                        successListener.onSuccess(snapshot != null && snapshot.exists());
                    }
                })
                .addOnFailureListener(error -> {
                    if (failureListener != null) {
                        failureListener.onFailure(error);
                    }
                });
    }

    /**
     * Updates the posterUrl field for the event document.
     */
    public void updatePosterUrl(@NonNull String eventId,
                                @NonNull String posterImageId,
                                @Nullable OnSuccessListener<Void> successListener,
                                @Nullable OnFailureListener failureListener) {
        firestore.collection(COLLECTION_EVENTS)
                .document(eventId)
                .update("posterUrl", posterImageId)
                .addOnSuccessListener(v -> {
                    if (successListener != null) successListener.onSuccess(v);
                })
                .addOnFailureListener(err -> {
                    if (failureListener != null) failureListener.onFailure(err);
                });
    }

    /**
     * Stores a QR code download URL in the event doc.
     *
     * @param eventId         id of the event document
     * @param payload         data to store under {@code qrPayload}
     * @param successListener optional success callback
     * @param failureListener optional failure callback
     */
    public void updateQrPayload(@NonNull String eventId,
                                @NonNull String payload,
                                @Nullable OnSuccessListener<Void> successListener,
                                @Nullable OnFailureListener failureListener) {
        firestore.collection(COLLECTION_EVENTS)
                .document(eventId)
                .update("qrPayload", payload)
                .addOnSuccessListener(v -> {
                    if (successListener != null) successListener.onSuccess(v);
                })
                .addOnFailureListener(err -> {
                    if (failureListener != null) failureListener.onFailure(err);
                });
    }

    /**
     * Deletes event with provided ID
     * @param eventId event ID
     * @param successListener executed on success
     * @param failureListener executed on failure
     */
    public void deleteEvent(@NonNull String eventId,
                            @Nullable OnSuccessListener<String> successListener,
                            @Nullable OnFailureListener failureListener) {
        firestore.collection(COLLECTION_EVENTS)
                .document(eventId)
                .delete()
                .addOnSuccessListener(v -> {
                    if (successListener != null) successListener.onSuccess(eventId);
                })
                .addOnFailureListener(err -> {
                    if (failureListener != null) failureListener.onFailure(err);
                });
    }


    /**
     * Converts a snapshot to models and forwards them to the optional listener.
     *
     * @param successListener optional callback for the mapped events
     * @param snapshot        Firestore query result
     * @return mapped list (never {@code null})
     */
    List<Event> dispatchEvents(@Nullable OnSuccessListener<List<Event>> successListener,
                               QuerySnapshot snapshot) {
        List<Event> events = mapEvents(snapshot);
        if (successListener != null) {
            successListener.onSuccess(events);
        }
        return events;
    }

    /**
     * Maps Firestore documents into {@link Event} instances.
     *
     * @param snapshot query result
     * @return mapped events list
     */
    public List<Event> mapEvents(@Nullable QuerySnapshot snapshot) {
        List<Event> events = new ArrayList<>();
        if (snapshot == null) {
            return events;
        }
        snapshot.getDocuments().forEach(document -> events.add(Event.fromSnapshot(document)));
        return events;
    }

    private void validateIds(@NonNull String eventId, @NonNull String entrantId) {
        if (isNullOrBlank(eventId) || isNullOrBlank(entrantId)) {
            throw new IllegalArgumentException("Event id and entrant id are required");
        }
    }

    private boolean isNullOrBlank(@Nullable String value) {
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
