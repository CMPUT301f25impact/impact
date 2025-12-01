package com.example.impact.model;

import androidx.annotation.Nullable;

import java.io.Serializable;

/**
 * Base class for all app users (entrants, organizers, admins).
 */
public abstract class User implements Serializable {
    private String id; // Unique id for Users
    private String email;
    @Nullable
    private String name;
    @Nullable
    private String phone;
    private boolean notificationsEnabled = true;

    public User() {
        // Default constructor for Firestore
    }

    /**
     * Creates an User profile using the provided details.
     *
     * @param id    unique identifier for the entrant
     * @param name  entrant full name
     * @param email entrant contact email
     * @param phone optional entrant phone number
     */
    public User(String id, @Nullable String name, String email, @Nullable String phone) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }
    /**
     * @return unique identifier for this profile
     */
    public String getId() {
        return id;
    }

    /**
     * @param id unique identifier for this profile
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return optional display name
     */
    @Nullable
    public String getName() {
        return name;
    }

    /**
     * @param name optional display name
     */
    public void setName(@Nullable String name) {
        this.name = name;
    }

    /**
     * @return contact email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email contact email address
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return optional phone number
     */
    @Nullable
    public String getPhone() {
        return phone;
    }

    public void setPhone(@Nullable String phone) {
        this.phone = phone;
    }

    /**
     * Indicates whether the user wishes to receive notifications.
     *
     * @return {@code true} when notifications are enabled
     */
    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    /**
     * Sets the notification preference for the user profile.
     *
     * @param notificationsEnabled {@code true} to opt in to notifications
     */
    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    /**
     * @return role string used for Firestore persistence
     */
    public abstract String getRole();
}
