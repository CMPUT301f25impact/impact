package com.example.impact.model;

import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

/**
 * Represents a notification sent from an organizer/admin to one or more entrants.
 */
public class Notification implements Serializable {

    private String id;

    private User sender;
    private ArrayList<User> recipients;
    @Nullable
    private Event related_event;
    private String message;
//    private String sys_message; // I'm thinking we might want this to get specific with notification types
    private Date time_stamp;

    public Notification() {
        // Default constructor
    }

    /**
     * Builds a notification payload and stamps the current time.
     */
    public Notification(String id, User sender, ArrayList<User> recipients, @Nullable Event related_event, String message) {
        this(id, sender, recipients, related_event, message, new Date());
    }

    /**
     * Builds a notification payload using an explicit timestamp (used for Firestore mapping).
     */
    public Notification(String id, User sender, ArrayList<User> recipients, @Nullable Event related_event, String message, Date time_stamp) {
        this.id = id;
        this.sender = sender;
        this.recipients = recipients;
        this.related_event = related_event;
        this.message = message;
        this.time_stamp = time_stamp;
    }

    /**
     * @return mutable list of recipients
     */
    public ArrayList<User> getRecipients() {
        return recipients;
    }

    /**
     * @return timestamp representing when the notification was created
     */
    public Date getTime_stamp() {
        return time_stamp;
    }

    /**
     * @return optional related event reference
     */
    @Nullable
    public Event getRelated_event() {
        return related_event;
    }

    /**
     * @param message notification text
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return notification body text
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return sender user context
     */
    public User getSender() {
        return sender;
    }

    /**
     * @return Firestore document id
     */
    public String getId() {return id; }

    // Entrant-friendly getters (do NOT remove old ones)

    /**
     * @return name of the related event when set
     */
    public String getEventName() {
        if (related_event != null) {
            return related_event.getName();
        }
        return null;
    }

    /**
     * @return alias for {@link #getTime_stamp()}
     */
    public Date getCreatedAt() {
        return time_stamp;
    }

}
