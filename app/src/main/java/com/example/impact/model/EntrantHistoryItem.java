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

    public EntrantHistoryItem(String eventId, String eventName, String status, Date timestamp) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.status = status;
        this.timestamp = timestamp;
    }

    public String getEventId() {
        return eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public String getStatus() {
        return status;
    }

    public Date getTimestamp() {
        return timestamp;
    }
}
