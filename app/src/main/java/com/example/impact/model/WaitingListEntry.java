package com.example.impact.model;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.Date;

/**
 * Represents an entrant's placement on an event waiting list.
 */
public class WaitingListEntry implements Serializable {
    private String eventId;
    private String eventName;
    private String entrantId;
    private String status;
    @ServerTimestamp
    private Date timestamp;

    /**
     * Required empty constructor for Firestore deserialization.
     */
    public WaitingListEntry() {
        // Default constructor for Firestore
    }

    /**
     * Creates a waiting list entry snapshot.
     *
     * @param eventId   associated event id
     * @param eventName associated event name
     * @param entrantId entrant id occupying the slot
     * @param timestamp time the record was created
     * @param status    current waiting-list status
     */
    public WaitingListEntry(String eventId, String eventName, String entrantId, Date timestamp, String status) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.entrantId = entrantId;
        this.timestamp = timestamp;
        this.status = status;
    }

    /**
     * @return parent event id
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * @param eventId parent event id
     */
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    /**
     * @return event name snapshot
     */
    public String getEventName() {
        return eventName;
    }

    /**
     * @param eventName event name snapshot
     */
    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    /**
     * @return entrant identifier stored in the record
     */
    public String getEntrantId() {
        return entrantId;
    }

    /**
     * @param entrantId entrant identifier stored in the record
     */
    public void setEntrantId(String entrantId) {
        this.entrantId = entrantId;
    }

    /**
     * @return timestamp assigned by Firestore
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * @param timestamp timestamp assigned by Firestore
     */
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * @return current waiting-list status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status current waiting-list status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Builds a waiting list entry from a snapshot, ensuring id integrity.
     *
     * @param snapshot Firestore document
     * @return populated WaitingListEntry
     */
    public static WaitingListEntry fromSnapshot(DocumentSnapshot snapshot) {
        WaitingListEntry entry = snapshot.toObject(WaitingListEntry.class);
        if (entry == null) {
            entry = new WaitingListEntry();
        }
        if (entry.getEntrantId() == null) {
            entry.setEntrantId(snapshot.getId());
        }
        if (entry.getEventId() == null) {
            DocumentReference parentRef = snapshot.getReference().getParent().getParent();
            if (parentRef != null) {
                entry.setEventId(parentRef.getId());
            }
        }
        return entry;
    }
}
