package com.example.impact.model;

import androidx.annotation.Nullable;

/**
 * Admin user model
 */
public class Admin extends User {

    public static final String ROLE_KEY = "admin";
    public Admin() {
        // Default constructor for Firestore
    }
    public Admin(String id, @Nullable String name, String email, @Nullable String phone) {
        super(id, name, email, phone);
    }

    @Override
    public String getRole() {
        return ROLE_KEY;
    }
}
