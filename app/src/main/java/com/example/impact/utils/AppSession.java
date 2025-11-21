package com.example.impact.utils;

import androidx.annotation.Nullable;

import com.example.impact.model.User;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Simple in-memory session store for the currently authenticated user and shared Firestore.
 */
public final class AppSession {

    private static final FirebaseFirestore db = FirebaseUtils.getFirestore();

    private static User currentUser;
    private static String currentUserId;
    private static String role;

    private AppSession() {
        // Utility class
    }

    /**
     * Populates the session state with the provided user model.
     *
     * @param user authenticated user (or {@code null} to clear the session)
     */
    public static void initialize(@Nullable User user) {
        currentUser = user;
        currentUserId = user != null ? user.getId() : null;
        role = user != null ? user.getRole() : null;
    }

    /**
     * @return currently cached user model
     */
    @Nullable
    public static User getUser() {
        return currentUser;
    }

    /**
     * @return identifier of the cached user, or {@code null}
     */
    @Nullable
    public static String getUserId() {
        return currentUserId;
    }

    /**
     * @return cached role for the authenticated user
     */
    @Nullable
    public static String getRole() {
        return role;
    }

    /**
     * @return shared Firestore instance for the app
     */
    public static FirebaseFirestore db() {
        return db;
    }
}
