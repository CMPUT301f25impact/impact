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

    /**
     * @return Firestore identifier
     */
    public String getId() {
        return id;
    }

    /**
     * @param id Firestore identifier
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return event display name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name event display name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return short event description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description summary describing the event
     */
    public void setDescription(String description) {
        this.description = description;
    }

    @Nullable
    /**
     * @return optional start date
     */
    public Date getStartDate() {
        return startDate;
    }

    /**
     * @param startDate optional start date
     */
    public void setStartDate(@Nullable Date startDate) {
        this.startDate = startDate;
    }

    @Nullable
    /**
     * @return optional end date
     */
    public Date getEndDate() {
        return endDate;
    }

    /**
     * @param endDate optional end date
     */
    public void setEndDate(@Nullable Date endDate) {
        this.endDate = endDate;
    }

    @Nullable
    /**
     * @return poster URL if one exists
     */
    public String getPosterUrl() {
        return posterUrl;
    }

    /**
     * @param posterUrl optional image URL
     */
    public void setPosterUrl(@Nullable String posterUrl) {
        this.posterUrl = posterUrl;
    }

    /**
     * @return mutable list of interest tags
     */
    public List<String> getTags() {
        return tags;
    }

    /**
     * @param tags interest tags (uses empty list when {@code null})
     */
    public void setTags(List<String> tags) {
        this.tags = tags != null ? tags : new ArrayList<>();
    }
    /**
     * @return optional QR payload URL
     */
    @Nullable
    public String getQrCodeUrl() {
        return qrCodeUrl;
    }

    /**
     * @param qrCodeUrl optional QR payload URL
     */
    public void setQrCodeUrl(@Nullable String qrCodeUrl) {
        this.qrCodeUrl = qrCodeUrl;
    }

    /**
     * @return organizer email tied to the event
     */
    @Nullable
    public String getOrganizerEmail() {
        return organizerEmail;
    }

    /**
     * @param organizerEmail organizer email tied to the event
     */
    public void setOrganizerEmail(@Nullable String organizerEmail) {
        this.organizerEmail = organizerEmail;
    }

    /**
     * @return optional capacity value
     */
    @Nullable
    public Integer getCapacity() {
        return capacity;
    }

    /**
     * @param capacity optional capacity value
     */
    public void setCapacity(@Nullable Integer capacity) {
        this.capacity = capacity;
    }

    /**
     * Populates an event from a Firestore snapshot.
     *
     * @param snapshot Firestore document
     * @return mapped Event instance
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
