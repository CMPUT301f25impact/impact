package com.example.impact.model;

import androidx.annotation.Nullable;

import java.io.Serializable;

public abstract class User implements Serializable {
    private String id; // Unique id for Users
    private String email;
    @Nullable
    private String name;
    @Nullable
    private String phone;

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
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Nullable
    public String getName() {
        return name;
    }

    public void setName(@Nullable String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Nullable
    public String getPhone() {
        return phone;
    }

    public void setPhone(@Nullable String phone) {
        this.phone = phone;
    }
}
