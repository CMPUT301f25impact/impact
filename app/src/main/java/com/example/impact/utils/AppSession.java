package com.example.impact.utils;

import androidx.annotation.Nullable;

import com.example.impact.model.User;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Central in-memory session store that exposes the authenticated {@link User},
 * the cached role/identifier, and the shared {@link FirebaseFirestore} instance.
 * <p>
 * The rest of the codebase (controllers, role utilities, and activities) should always
 * obtain session context from this class instead of re-querying Firestore or passing user
 * details between screens. Firestore access should flow through {@link #db()} so that every
 * role utility works with the same configured instance.
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
     * Initializes or clears the session with the supplied user.
     *
     * @param user authenticated user (or {@code null} to clear the stored state)
     */
    public static void initialize(@Nullable User user) {
        currentUser = user;
        currentUserId = user != null ? user.getId() : null;
        role = user != null ? user.getRole() : null;
    }

    /**
     * Returns the currently cached user (if any).
     *
     * @return authenticated {@link User} or {@code null} when no one is logged in
     */
    @Nullable
    public static User getUser() {
        ensureUserAvailable();
        return currentUser;
    }

    /**
     * Returns the cached Firestore document id for the authenticated user.
     *
     * @return user id or {@code null} when the session has been cleared
     */
    @Nullable
    public static String getUserId() {
        ensureSessionInitialized();
        if (currentUserId == null && currentUser != null) {
            currentUserId = currentUser.getId();
        }
        if (currentUserId == null) {
            throw new IllegalStateException("AppSession user id is unavailable. Initialize the session before accessing it.");
        }
        return currentUserId;
    }

    /**
     * Returns the cached role key (entrant/organizer/admin) for the authenticated user.
     *
     * @return role string or {@code null} when no session is active
     */
    @Nullable
    public static String getRole() {
        ensureSessionInitialized();
        if (role == null && currentUser != null) {
            role = currentUser.getRole();
        }
        if (role == null) {
            throw new IllegalStateException("AppSession role is unavailable. Initialize the session before accessing it.");
        }
        return role;
    }

    /**
     * Exposes the shared {@link FirebaseFirestore} configured for the application.
     * Role utilities should always call this method rather than instantiating their own
     * Firestore references so that every query and write uses the same configuration.
     *
     * @return shared Firestore instance
     */
    public static FirebaseFirestore db() {
        return db;
    }

    /**
     * Ensures a user object exists before returning it to callers.
     */
    private static void ensureUserAvailable() {
        ensureSessionInitialized();
        if (currentUser == null) {
            throw new IllegalStateException("AppSession user is unavailable. Initialize the session before accessing it.");
        }
    }

    /**
     * Guards getters so Activities/Fragments do not need to duplicate null checks.
     */
    private static void ensureSessionInitialized() {
        if (currentUser == null && currentUserId == null && role == null) {
            throw new IllegalStateException("AppSession has not been initialized.");
        }
    }
}
