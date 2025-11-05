package com.example.impact.utils;

import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Utility helpers for accessing configured Firebase services.
 */
public final class FirebaseUtil {

    private FirebaseUtil() {
        // Utility class
    }

    /**
     * Provides a singleton instance of {@link FirebaseFirestore} configured for the app.
     *
     * @return shared Firestore instance
     */
    public static FirebaseFirestore getFirestore() {
        return FirebaseFirestore.getInstance();
    }
}
