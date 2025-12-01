package com.example.impact.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Represents a single history record for an entrant.
 */
public class EntrantHistoryItem implements Serializable {
    private String eventId;
    private String eventName;
    private String status;
    private Date timestamp;

    /**
     * Creates an immutable history entry.
     *
     * @param eventId   associated event id
     * @param eventName display name of the event
     * @param status    waiting list status at that time
     * @param timestamp when the status was recorded
     */
    public EntrantHistoryItem(String eventId, String eventName, String status, Date timestamp) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.status = status;
        this.timestamp = timestamp;
    }

    /**
     * @return event identifier this record references
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * @return event title for display
     */
    public String getEventName() {
        return eventName;
    }

    /**
     * @return waiting-list status label
     */
    public String getStatus() {
        return status;
    }

    /**
     * @return timestamp of the waiting-list action, or {@code null}
     */
    public Date getTimestamp() {
        return timestamp;
    }
}
