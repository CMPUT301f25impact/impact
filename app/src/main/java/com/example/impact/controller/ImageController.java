package com.example.impact.controller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.impact.model.Event;
import com.example.impact.model.Image;
import com.example.impact.utils.FirebaseUtil;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Handle image retrieval and saving to Firestore
 */
public class ImageController {

    private static final String COLLECTION_IMAGES = "images";
    private final FirebaseFirestore firestore;

    public ImageController() {
        this(FirebaseUtil.getFirestore());
    }

    public ImageController(@NonNull FirebaseFirestore firestore) {
        this.firestore = firestore;
    }

    /**
     * Gets all images
     * @param successListener success callback
     * @param failureListener failure callback
     */
    public void fetchAllImages(@Nullable OnSuccessListener<List<Image>> successListener,
                                     @Nullable OnFailureListener failureListener) {
        firestore.collection(COLLECTION_IMAGES)
                .get()
                .addOnSuccessListener(snapshot -> dispatchImages(successListener, snapshot))
                .addOnFailureListener(error -> {
                    if (failureListener != null) {
                        failureListener.onFailure(error);
                    }
                });
    }

    /**
     * Fetches a single image by id
     * @param imageId image id
     * @param successListener success callback
     * @param failureListener failure callback
     */
    public void fetchImage(@NonNull String imageId,
                           @Nullable OnSuccessListener<Image> successListener,
                           @Nullable OnFailureListener failureListener) {
        firestore.collection(COLLECTION_IMAGES)
                .document(imageId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (successListener != null) {
                        successListener.onSuccess(Image.fromSnapshot(snapshot));
                    }
                })
                .addOnFailureListener(error -> {
                    if (failureListener != null) {
                        failureListener.onFailure(error);
                    }
                });
    }

    /**
     * Creates an image document. The Image object should have:
     * image id, mime image type, file name, and base64 encoded content
     * Returns the new document id via successListener.
     * @param image image object to store
     * @param successListener success callback
     * @param failureListener failure callback
     */
    public void createImage(@NonNull Image image,
                            @Nullable OnSuccessListener<String> successListener,
                            @Nullable OnFailureListener failureListener) {
        firestore.collection(COLLECTION_IMAGES)
                .add(image)
                .addOnSuccessListener(ref -> {
                    if (successListener != null) successListener.onSuccess(ref.getId());
                })
                .addOnFailureListener(err -> {
                    if (failureListener != null) failureListener.onFailure(err);
                });
    }

    /**
     * Deletes image with provided ID
     * @param imageId image ID
     * @param successListener executed on success
     * @param failureListener executed on failure
     */
    public void deleteImage(@NonNull String imageId,
                            @Nullable OnSuccessListener<String> successListener,
                            @Nullable OnFailureListener failureListener) {
        firestore.collection(COLLECTION_IMAGES)
                .document(imageId)
                .delete()
                .addOnSuccessListener(v -> {
                    if (successListener != null) successListener.onSuccess(imageId);
                })
                .addOnFailureListener(err -> {
                    if (failureListener != null) failureListener.onFailure(err);
                });
    }

    private List<Image> dispatchImages(@Nullable OnSuccessListener<List<Image>> successListener,
                               QuerySnapshot snapshot) {
        List<Image> images = mapImages(snapshot);
        if (successListener != null) {
            successListener.onSuccess(images);
        }
        return images;
    }

    private List<Image> mapImages(@Nullable QuerySnapshot snapshot) {
        List<Image> images = new ArrayList<>();
        if (snapshot == null) {
            return images;
        }
        snapshot.getDocuments().forEach(document -> images.add(Image.fromSnapshot(document)));
        return images;
    }
}
