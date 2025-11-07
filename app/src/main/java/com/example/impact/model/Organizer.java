package com.example.impact.model;

import androidx.annotation.Nullable;

/**
 * Organizer profile model
 */
public class Organizer extends User {

    public static final String ROLE_KEY = "organizer";
    public Organizer() {
        // Default constructor for Firestore
    }

    public Organizer(String id, @Nullable String name, String email, @Nullable String phone) {
        super(id, name, email, phone);
    }

    @Override
    public String getRole() {
        return ROLE_KEY;
    }
}
