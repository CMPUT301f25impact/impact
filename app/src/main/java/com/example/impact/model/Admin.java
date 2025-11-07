package com.example.impact.model;

import androidx.annotation.Nullable;

/**
 * Admin user model
 */
public class Admin extends User {
    public Admin() {
        // Default constructor for Firestore
    }
    public Admin(String id, @Nullable String name, String email, @Nullable String phone) {
        super(id, name, email, phone);
    }

}
