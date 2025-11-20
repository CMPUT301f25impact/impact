package com.example.impact.utils;

import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Utility helpers for accessing configured Firebase services.
 * Devs should use the data model classes instead of directly using these methods.
 */
public final class FirebaseUtils {
    private static final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    // ^^ False positive memory leak warning

    private static final OnSuccessListener<Void> DEFAULT_SUCCESS_HANDLER = aVoid -> Log.d("Firestore", "Operation successful");
    private static final OnFailureListener DEFAULT_ERROR_HANDLER = error -> Log.e("Firestore", "Error: " + error.getMessage());

    private FirebaseUtils() {
        // Utility class
    }

    /**
     * Provides a singleton instance of {@link FirebaseFirestore} configured for the app.
     * Should avoid using this method
     *
     * @return shared Firestore instance
     */
    public static FirebaseFirestore getFirestore() {
        return firestore;
    }

    /**
     * Add a document to a collection with auto-generated ID.
     *
     * @param collection Collection name
     * @param data Data object to store
     * @param onSuccess Callback with generated document ID
     * @param onFailure Callback for errors
     */
    public static void createDocument(String collection, Object data, OnSuccessListener<String> onSuccess, OnFailureListener onFailure) {
        firestore.collection(collection)
                .add(data)
                .addOnSuccessListener(docRef -> {
                    if (onSuccess != null) {
                        onSuccess.onSuccess(docRef.getId());
                    }
                })
                .addOnFailureListener(e -> {
                    if (onFailure != null) {
                        onFailure.onFailure(e);
                    }
                });
    }

    /**
     * Creates or overwrites a document with a specific ID.
     * Will delete fields not in the provided object
     *
     * @param collection Collection name
     * @param documentId Document ID
     * @param data Data object to store
     * @param onSuccess Success callback
     * @param onFailure Failure callback
     */
    public static void setDocument(String collection, String documentId, Object data, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        firestore.collection(collection)
                .document(documentId)
                .set(data)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    /**
     * Set a document with merge option (updates existing fields, adds new ones).
     * Leaves old fields as is.
     *
     * @param collection Collection name
     * @param documentId Document ID
     * @param data Data object to store
     * @param onSuccess Success callback
     * @param onFailure Failure callback
     */
    public static void setMergeDocument(String collection, String documentId, Object data,
                                        OnSuccessListener<Void> onSuccess,
                                        OnFailureListener onFailure) {
        firestore.collection(collection)
                .document(documentId)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    /**
     * Get a document reference (useful for subcollections or batch operations).
     *
     * @param collection Collection name
     * @param documentId Document ID
     * @return DocumentReference
     */
    public static DocumentReference getDocumentReference(String collection, String documentId) {
        return firestore.collection(collection).document(documentId);
    }

    /**
     * Create a new WriteBatch for batch operations.
     *
     * @return WriteBatch instance
     */
    public static WriteBatch createBatch() {
        return firestore.batch();
    }

    /**
     * Get a single document by ID.
     *
     * @param collection Collection name
     * @param documentId Document ID
     * @param onSuccess Callback with DocumentSnapshot
     * @param onFailure Failure callback
     */
    public static void getDocument(String collection, String documentId, OnSuccessListener<DocumentSnapshot> onSuccess, OnFailureListener onFailure) {
        firestore.collection(collection)
                .document(documentId)
                .get()
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    /**
     * Get a document by ID and collection and convert to a class.
     *
     * @param collection Collection name
     * @param documentId Document ID
     * @param clazz Class to convert to
     * @param onSuccess Callback with converted object (object is null if document doesn't exist)
     * @param onFailure Failure callback
     */
    /*
    Example usage:

    FirestoreUtils.getDocument("users", userId, User.class,
    user -> {
        if (user != null) {
            // Use the user object
        }
    },
    error -> Log.e("Firestore", "Error: " + error.getMessage()));

     */
    public static <T> void getDocument(String collection, String documentId, Class<T> clazz, OnSuccessListener<T> onSuccess, OnFailureListener onFailure) {
        firestore.collection(collection)
                .document(documentId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (onSuccess != null) {
                        T obj = snapshot.exists() ? snapshot.toObject(clazz) : null;
                        onSuccess.onSuccess(obj);
                    }
                })
                .addOnFailureListener(onFailure);
    }

    /**
     * Query documents in a given collection by a single where clause and convert results into a list of class objects
     *
     * @param collection Collection name
     * @param field Field name to query
     * @param value Value to match
     * @param clazz Class to convert documents to
     * @param onSuccess Callback with list of objects
     * @param onFailure Failure callback
     */
    /*
    Example usage:

    FirestoreUtils.queryDocuments("users", "deviceId", deviceId,
    snapshot -> {
        if (snapshot.isEmpty()) {
            proceedToLogin(false);
            return;
        }
        DocumentSnapshot userDoc = snapshot.getDocuments().get(0);
        proceedToRole(userDoc);
    },
    error -> proceedToLogin(true));

     */
    public static <T> void queryDocuments(String collection, String field, Object value, Class<T> clazz, OnSuccessListener<List<T>> onSuccess, OnFailureListener onFailure) {
        firestore.collection(collection)
                .whereEqualTo(field, value)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (onSuccess != null) {
                        List<T> list = new ArrayList<>();
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            T obj = doc.toObject(clazz);
                            if (obj != null) {
                                list.add(obj);
                            }
                        }
                        onSuccess.onSuccess(list);
                    }
                })
                .addOnFailureListener(onFailure);
    }

    /**
     * Update specific fields in a document.
     *
     * @param collection Collection name
     * @param documentId Document ID
     * @param updates Map of field names to new values
     * @param onSuccess Success callback
     * @param onFailure Failure callback
     */
    /*
    Example usage:

    Map<String, Object> updates = new HashMap<>();
    updates.put("name", "Jane Doe");
    updates.put("age", 26);
    FirestoreUtils.updateDocument("users", userId, updates,
        aVoid -> Log.d("Firestore", "User updated"),
        error -> Log.e("Firestore", "Error: " + error.getMessage()));

     */
    public static void updateDocument(String collection, String documentId, Map<String, Object> updates, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        firestore.collection(collection)
                .document(documentId)
                .update(updates)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    /**
     * Delete a document.
     *
     * @param collection Collection name
     * @param documentId Document ID
     * @param onSuccess Success callback
     * @param onFailure Failure callback
     */
    public static void deleteDocument(String collection, String documentId, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        firestore.collection(collection)
                .document(documentId)
                .delete()
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    /**
     * Delete all documents matching a query.
     *
     * @param collection Collection name
     * @param field Field name to query
     * @param value Value to match
     * @param onSuccess Callback with count of deleted documents
     * @param onFailure Failure callback
     */
    public static void deleteDocumentsByQuery(String collection, String field, Object value, OnSuccessListener<Integer> onSuccess, OnFailureListener onFailure) {
        firestore.collection(collection)
                .whereEqualTo(field, value)
                .get()
                .addOnSuccessListener(snapshot -> {
                    WriteBatch batch = firestore.batch();
                    int count = 0;
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        batch.delete(doc.getReference());
                        count++;
                    }
                    final int deletedCount = count;
                    batch.commit()
                            .addOnSuccessListener(aVoid -> {
                                if (onSuccess != null) {
                                    onSuccess.onSuccess(deletedCount);
                                }
                            })
                            .addOnFailureListener(onFailure);
                })
                .addOnFailureListener(onFailure);
    }

    /**
     * Check if a document exists.
     *
     * @param collection Collection name
     * @param documentId Document ID
     * @param onSuccess Callback with boolean result (true if exists)
     * @param onFailure Failure callback
     */
    public static void documentExists(String collection, String documentId, OnSuccessListener<Boolean> onSuccess, OnFailureListener onFailure) {
        firestore.collection(collection)
                .document(documentId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (onSuccess != null) {
                        onSuccess.onSuccess(snapshot.exists());
                    }
                })
                .addOnFailureListener(onFailure);
    }


}
