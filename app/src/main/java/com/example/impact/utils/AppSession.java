package com.example.impact.utils;

import android.content.Intent;

import androidx.annotation.Nullable;

import com.example.impact.model.User;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Simple in-memory session store for the currently authenticated user and shared Firestore.
 */
public final class AppSession {

    private static final FirebaseFirestore db = FirebaseUtils.getFirestore();

    private static final String EXTRA_USER_ID = "extra_user_id";
    private static final String EXTRA_USER_EMAIL = "extra_user_email";

    private static Intent startupIntent;
    private static User currentUser;
    private static String currentUserId;
    private static String role;
    private static String email;

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
        email = user != null ? user.getEmail() : null;
        if (user == null) {
            startupIntent = null;
        }
    }

    /**
     * Captures the launching intent so AppSession can hydrate itself if the process was killed.
     */
    public static void setStartupIntent(@Nullable Intent intent) {
        startupIntent = intent;
    }

    /**
     * @return currently cached user model
     */
    @Nullable
    public static User getUser() {
        hydrateFromIntent();
        return currentUser;
    }

    /**
     * @return identifier of the cached user, or {@code null}
     */
    @Nullable
    public static String getUserId() {
        hydrateFromIntent();
        return currentUserId;
    }

    /**
     * @return cached email address for the authenticated user
     */
    @Nullable
    public static String getEmail() {
        hydrateFromIntent();
        return email;
    }

    /**
     * @return cached role for the authenticated user
     */
    @Nullable
    public static String getRole() {
        hydrateFromIntent();
        return role;
    }

    /**
     * @return shared Firestore instance for the app
     */
    public static FirebaseFirestore db() {
        return db;
    }

    private static void hydrateFromIntent() {
        if (startupIntent == null) {
            return;
        }
        if (currentUserId == null) {
            currentUserId = startupIntent.getStringExtra(EXTRA_USER_ID);
        }
        if (email == null) {
            email = startupIntent.getStringExtra(EXTRA_USER_EMAIL);
        }
    }
}
