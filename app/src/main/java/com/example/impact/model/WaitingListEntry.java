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

    public WaitingListEntry(String eventId, String eventName, String entrantId, Date timestamp, String status) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.entrantId = entrantId;
        this.timestamp = timestamp;
        this.status = status;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEntrantId() {
        return entrantId;
    }

    public void setEntrantId(String entrantId) {
        this.entrantId = entrantId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Builds a waiting list entry from a snapshot, ensuring id integrity.
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
