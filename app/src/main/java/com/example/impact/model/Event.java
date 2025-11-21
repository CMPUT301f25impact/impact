package com.example.impact.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.DocumentSnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Represents an event stored under the root {@code events/} collection.
 * Includes helpers for dealing with nested image subcollections created in Step B of the refactor.
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

/**
 * @return optional start date
 */
@Nullable
public Date getStartDate() {
        return startDate;
    }

    /**
     * @param startDate optional start date
     */
    public void setStartDate(@Nullable Date startDate) {
        this.startDate = startDate;
    }

/**
 * @return optional end date
 */
@Nullable
public Date getEndDate() {
        return endDate;
    }

    /**
     * @param endDate optional end date
     */
    public void setEndDate(@Nullable Date endDate) {
        this.endDate = endDate;
    }

/**
 * @return poster URL if one exists
 */
@Nullable
public String getPosterUrl() {
        return posterUrl;
    }

    /**
     * @return identifier of the poster image document (last path segment under {@code events/{id}/images})
     */
    @Nullable
    public String getPosterImageId() {
        return extractImageId(posterUrl);
    }

    /**
     * @param posterUrl optional image URL/path
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
     * @return identifier of the QR image document (last path segment under {@code events/{id}/images})
     */
    @Nullable
    public String getQrImageId() {
        return extractImageId(qrCodeUrl);
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
     * Populates an event from a Firestore snapshot ensuring {@link #getId()} reflects the document id
     * even when {@link com.google.firebase.firestore.DocumentSnapshot#toObject(Class)} omits it.
     *
     * @param snapshot Firestore document
     * @return mapped Event instance
     */
    public static Event fromSnapshot(DocumentSnapshot doc) {
        Event e = doc.toObject(Event.class);
        if (e == null) {
            e = new Event();
        }
        // set the canonical document id (so e.getId() returns the Firestore doc id)
        e.setId(doc.getId());

        // Firestore field name for poster you used is posterUrl
        // but .toObject may or may not map it depending on naming.
        // Ensure falling back to reading field directly:
        if (doc.contains("posterUrl")) {
            String poster = doc.getString("posterUrl");
            e.setPosterUrl(poster);
        }

        return e;
    }

    /**
     * Builds a canonical Firestore path for an image nested under {@code events/{eventId}/images/}.
     *
     * @param eventId event identifier
     * @param imageDocumentId image document id
     * @return canonical path string (e.g., {@code events/{eventId}/images/{imageDocumentId}})
     */
    public static String buildImagePath(@NonNull String eventId, @NonNull String imageDocumentId) {
        return "events/" + eventId + "/images/" + imageDocumentId;
    }

    /**
     * Extracts the image document id from an event-scoped path such as {@code events/{id}/images/poster}.
     *
     * @param imagePath canonical Firestore path
     * @return image document id or {@code null} when the path is empty
     */
    @Nullable
    public static String extractImageId(@Nullable String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return null;
        }
        int slash = imagePath.lastIndexOf('/');
        if (slash == -1 || slash == imagePath.length() - 1) {
            return imagePath;
        }
        return imagePath.substring(slash + 1);
    }
}
