package com.example.impact.controller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.impact.model.Event;
import com.example.impact.utils.AppSession;
import com.example.impact.utils.role.AdminDb;
import com.example.impact.utils.role.EntrantDb;
import com.example.impact.utils.role.OrganizerDb;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Coordinates event reads/writes between the UI layer and the role utilities.
 * <p>
 * All Firestore access is performed through {@link EntrantDb}, {@link OrganizerDb}, or {@link AdminDb};
 * the controller simply orchestrates which helper to call for the current user role.
 */
public class EventController {

    /**
     * Loads all events through the appropriate role utility (admins see every event, entrants use the entrant view).
     *
     * @param successListener invoked with the mapped events list
     * @param failureListener invoked when the Firestore read fails
     */
    public void fetchAvailableEvents(@Nullable OnSuccessListener<List<Event>> successListener,
                                     @Nullable OnFailureListener failureListener) {
        Task<List<Event>> task;
        if ("admin".equals(AppSession.getRole())) {
            task = AdminDb.listAllEvents();
        } else {
            task = EntrantDb.listAllEvents();
        }
        attach(task, successListener, failureListener);
    }

    /**
     * Performs a filtered fetch constrained by tags and date range via {@link EntrantDb#listFilteredEvents(List, Date, Date)}.
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
        Task<List<Event>> task = EntrantDb.listFilteredEvents(tags, startDate, endDate);
        attach(task, successListener, failureListener);
    }

    /**
     * Fetches all events that belong to the provided organizer email.
     *
     * @param organizerEmail organizer address stored on event documents
     * @param success        invoked with the organizer's events
     * @param failure        invoked when the read fails
     */
    public void fetchEventsByOrganizer(@NonNull String organizerEmail,
                                       @Nullable OnSuccessListener<List<Event>> success,
                                       @Nullable OnFailureListener failure) {
        Task<List<Event>> task = OrganizerDb.fetchEventsByEmail(organizerEmail);
        attach(task, success, failure);
    }

    /**
     * Subscribes to real-time updates for events owned by the supplied organizer email via {@link OrganizerDb}.
     *
     * @param email organizer email address
     * @param listener callback invoked with snapshot updates
     * @return listener registration to dispose of updates
     */
    public ListenerRegistration listenEventsByOrganizer(
            @NonNull String email,
            @NonNull EventListener<QuerySnapshot> listener) {

        return OrganizerDb.listenToEventsByEmail(email, listener);
    }


    /**
     * Creates an event document through {@link OrganizerDb#createEvent(Event)}.
     *
     * @param event           event model to persist
     * @param successListener invoked with the generated document id
     * @param failureListener invoked when the write fails
     */
    public void createEvent(@NonNull Event event,
                            @Nullable OnSuccessListener<String> successListener,
                            @Nullable OnFailureListener failureListener) {
        Task<String> task = OrganizerDb.createEvent(event);
        attach(task, successListener, failureListener);
    }

    /**
     * Updates the {@code posterUrl} field on {@code events/{eventId}} via {@link OrganizerDb#updateEvent(String, Map)}.
     */
    public void updatePosterUrl(@NonNull String eventId,
                                @NonNull String posterImageId,
                                @Nullable OnSuccessListener<Void> successListener,
                                @Nullable OnFailureListener failureListener) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("posterUrl", posterImageId);
        Task<Void> task = OrganizerDb.updateEvent(eventId, updates);
        attach(task, successListener, failureListener);
    }

    /**
     * Stores the QR payload string on the event document via {@link OrganizerDb}.
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
        Map<String, Object> updates = new HashMap<>();
        updates.put("qrPayload", payload);
        Task<Void> task = OrganizerDb.updateEvent(eventId, updates);
        attach(task, successListener, failureListener);
    }

    /**
     * Deletes {@code events/{eventId}} using {@link AdminDb#deleteEvent(String)}.
     *
     * @param eventId event ID
     * @param successListener executed on success
     * @param failureListener executed on failure
     */
    public void deleteEvent(@NonNull String eventId,
                            @Nullable OnSuccessListener<String> successListener,
                            @Nullable OnFailureListener failureListener) {
        Task<Void> task = AdminDb.deleteEvent(eventId);
        if (successListener != null) {
            task.addOnSuccessListener(unused -> successListener.onSuccess(eventId));
        }
        if (failureListener != null) {
            task.addOnFailureListener(failureListener);
        }
    }


    /**
     * Maps a {@link QuerySnapshot} into strongly typed {@link Event} objects, ensuring the id field is set.
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

    /**
     * Convenience helper to apply optional success/failure listeners to a {@link Task}.
     */
    private <T> void attach(Task<T> task,
                            @Nullable OnSuccessListener<T> successListener,
                            @Nullable OnFailureListener failureListener) {
        if (successListener != null) {
            task.addOnSuccessListener(successListener);
        }
        if (failureListener != null) {
            task.addOnFailureListener(failureListener);
        }
    }

}
