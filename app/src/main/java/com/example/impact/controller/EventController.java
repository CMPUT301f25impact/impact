package com.example.impact.controller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.impact.model.Event;
import com.example.impact.utils.FirebaseUtil;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Handles event retrieval and filtering logic between Firestore and the UI.
 */
public class EventController {
    private static final String COLLECTION_EVENTS = "events";

    private final FirebaseFirestore firestore;

    public EventController() {
        this(FirebaseUtil.getFirestore());
    }

    public EventController(@NonNull FirebaseFirestore firestore) {
        this.firestore = firestore;
    }

    /**
     * Loads all available events.
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
     * Fetches a single event by id.
     */
    public void fetchEvent(@NonNull String eventId,
                           @Nullable OnSuccessListener<Event> successListener,
                           @Nullable OnFailureListener failureListener) {
        firestore.collection(COLLECTION_EVENTS)
                .document(eventId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (successListener != null) {
                        successListener.onSuccess(Event.fromSnapshot(snapshot));
                    }
                })
                .addOnFailureListener(error -> {
                    if (failureListener != null) {
                        failureListener.onFailure(error);
                    }
                });
    }

    /**
     * Creates an event document. The Event object should have:
     * name, description, startDate, endDate, (optional) capacity, organizerId.
     * Returns the new document id via successListener.
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
     * Stores a QR code download URL in the event doc.
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


    List<Event> dispatchEvents(@Nullable OnSuccessListener<List<Event>> successListener,
                               QuerySnapshot snapshot) {
        List<Event> events = mapEvents(snapshot);
        if (successListener != null) {
            successListener.onSuccess(events);
        }
        return events;
    }

    List<Event> mapEvents(@Nullable QuerySnapshot snapshot) {
        List<Event> events = new ArrayList<>();
        if (snapshot == null) {
            return events;
        }
        snapshot.getDocuments().forEach(document -> events.add(Event.fromSnapshot(document)));
        return events;
    }
}
