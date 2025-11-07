package com.example.impact.model;

import androidx.annotation.Nullable;

import java.io.Serializable;

/**
 * Represents an entrant using the app and stores their profile information.
 */
public class Entrant implements Serializable {
    private String id;
    private String name;
    private String email;
    @Nullable
    private String phone;

    /**
     * Required empty constructor for Firestore deserialization.
     */
    public Entrant() {
        // Default constructor required for calls to DataSnapshot.getValue(Entrant.class)
    }

    /**
     * Creates an entrant profile using the provided details.
     *
     * @param id    unique identifier for the entrant
     * @param name  entrant full name
     * @param email entrant contact email
     * @param phone optional entrant phone number
     */
    public Entrant(String id, String name, String email, @Nullable String phone) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    /**
     * @return unique identifier for the entrant
     */
    public String getId() {
        return id;
    }

    /**
     * @param id unique identifier to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return full name for display
     */
    public String getName() {
        return name;
    }

    /**
     * @param name updated entrant name
     */
    public void setName(String name) {
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

    @Nullable
    /**
     * @return optional phone number, or {@code null}
     */
    public String getPhone() {
        return phone;
    }

    /**
     * @param phone optional phone number (may be {@code null})
     */
    public void setPhone(@Nullable String phone) {
        this.phone = phone;
    }
}
