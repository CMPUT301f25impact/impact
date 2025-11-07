package com.example.impact.model;

import androidx.annotation.Nullable;

import java.io.Serializable;

/**
 * Represents an entrant using the app and stores their profile information.
 */
public class Entrant extends User {
    private String name; // This is not Nullable for entrants

    public Entrant() {
        // Default constructor for Firestore
    }

    public Entrant(String id, String name, String email, @Nullable String phone) {
        super(id, name, email, phone);
    }
}
