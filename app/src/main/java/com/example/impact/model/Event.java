package com.example.impact.model;

import androidx.annotation.Nullable;

import com.google.firebase.firestore.DocumentSnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Represents an event that entrants can view and join waiting lists for.
 */
public class Event implements Serializable {
    private String id;
    private String name;
    private String description;
    @Nullable
    private Date startDate;
    @Nullable
    private Date endDate;
    @Nullable
    private String posterUrl;
    private List<String> tags = new ArrayList<>();
    private String qrCodeUrl;
    private String organizerEmail;
    private Integer capacity;

    /**
     * Required empty constructor for Firestore deserialization.
     */
    public Event() {
        // Default constructor for Firestore
    }

    /**
     * Creates an event with the supplied values.
     *
     * @param id          Firestore identifier
     * @param name        display name
     * @param description summary of the event
     * @param startDate   optional start time
     * @param endDate     optional end time
     * @param posterUrl   optional image url
     * @param tags        optional interest tags
     */
    public Event(String id,
                 String name,
                 String description,
                 @Nullable Date startDate,
                 @Nullable Date endDate,
                 @Nullable String posterUrl,
                 @Nullable List<String> tags) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.posterUrl = posterUrl;
        if (tags != null) {
            this.tags = tags;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Nullable
    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(@Nullable Date startDate) {
        this.startDate = startDate;
    }

    @Nullable
    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(@Nullable Date endDate) {
        this.endDate = endDate;
    }

    @Nullable
    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(@Nullable String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags != null ? tags : new ArrayList<>();
    }
    @Nullable
    public String getQrCodeUrl() { return qrCodeUrl; }
    public void setQrCodeUrl(@Nullable String qrCodeUrl) { this.qrCodeUrl = qrCodeUrl; }

    @Nullable
    public String getOrganizerEmail() { return organizerEmail; }
    public void setOrganizerEmail(@Nullable String organizerEmail) { this.organizerEmail = organizerEmail; }

    @Nullable
    public Integer getCapacity() { return capacity; }
    public void setCapacity(@Nullable Integer capacity) { this.capacity = capacity; }
    /**
     * Populates an event from a Firestore snapshot.
     */
    public static Event fromSnapshot(DocumentSnapshot snapshot) {
        Event event = snapshot.toObject(Event.class);
        if (event == null) {
            event = new Event();
        }
        event.setId(snapshot.getId());
        return event;
    }
}
