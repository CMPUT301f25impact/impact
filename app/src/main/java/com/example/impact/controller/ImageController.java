package com.example.impact.controller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.impact.model.Image;
import com.example.impact.utils.AppSession;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Handle image retrieval and saving to Firestore
 */
public class ImageController {

    private static final String COLLECTION_EVENTS = "events";
    private static final String SUB_COLLECTION_IMAGES = "images";
    private final FirebaseFirestore firestore;

    /**
     * Creates a controller using the shared Firestore instance.
     */
    public ImageController() {
        this(AppSession.db());
    }

    /**
     * Allows injection of a Firestore instance (primarily for tests).
     *
     * @param firestore backing store
     */
    public ImageController(@Nullable FirebaseFirestore firestore) {
        this.firestore = firestore != null ? firestore : AppSession.db();
    }

    /**
     * Retrieves every stored image document for the provided event.
     *
     * @param eventId         parent event identifier
     * @param successListener invoked with decoded images
     * @param failureListener invoked if the read fails
     */
    public void fetchAllImages(@NonNull String eventId,
                               @Nullable OnSuccessListener<List<Image>> successListener,
                               @Nullable OnFailureListener failureListener) {
        imageCollection(eventId)
                .get()
                .addOnSuccessListener(snapshot -> dispatchImages(eventId, successListener, snapshot))
                .addOnFailureListener(error -> {
                    if (failureListener != null) {
                        failureListener.onFailure(error);
                    }
                });
    }

    /**
     * Fetches a single image by id.
     *
     * @param eventId         parent event identifier
     * @param imageId         Firestore document id
     * @param successListener invoked with the decoded image
     * @param failureListener invoked if the read fails
     */
    public void fetchImage(@NonNull String eventId,
                           @NonNull String imageId,
                           @Nullable OnSuccessListener<Image> successListener,
                           @Nullable OnFailureListener failureListener) {
        imageCollection(eventId)
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
     * Creates or overwrites an image document under the event's image collection.
     *
     * @param eventId         parent event identifier
     * @param image           image object to store
     * @param successListener invoked with the document id used
     * @param failureListener invoked if the write fails
     */
    public void createImage(@NonNull String eventId,
                            @NonNull Image image,
                            @Nullable OnSuccessListener<String> successListener,
                            @Nullable OnFailureListener failureListener) {
        image.setEventId(eventId);
        CollectionReference images = imageCollection(eventId);
        String desiredId = sanitizeId(image.getImageId());
        if (desiredId != null) {
            images.document(desiredId)
                    .set(image)
                    .addOnSuccessListener(unused -> {
                        image.setImageId(desiredId);
                        if (successListener != null) successListener.onSuccess(desiredId);
                    })
                    .addOnFailureListener(err -> {
                        if (failureListener != null) failureListener.onFailure(err);
                    });
        } else {
            images.add(image)
                    .addOnSuccessListener(ref -> {
                        image.setImageId(ref.getId());
                        if (successListener != null) successListener.onSuccess(ref.getId());
                    })
                    .addOnFailureListener(err -> {
                        if (failureListener != null) failureListener.onFailure(err);
                    });
        }
    }

    /**
     * Deletes an image stored under the event.
     *
     * @param eventId         parent event identifier
     * @param imageId         document id to remove
     * @param successListener invoked with the deleted id
     * @param failureListener invoked if the delete fails
     */
    public void deleteImage(@NonNull String eventId,
                            @NonNull String imageId,
                            @Nullable OnSuccessListener<String> successListener,
                            @Nullable OnFailureListener failureListener) {
        imageCollection(eventId)
                .document(imageId)
                .delete()
                .addOnSuccessListener(v -> {
                    if (successListener != null) successListener.onSuccess(imageId);
                })
                .addOnFailureListener(err -> {
                    if (failureListener != null) failureListener.onFailure(err);
                });
    }

    private CollectionReference imageCollection(@NonNull String eventId) {
        return firestore.collection(COLLECTION_EVENTS)
                .document(eventId)
                .collection(SUB_COLLECTION_IMAGES);
    }

    /**
     * Routes mapped images to the optional success listener.
     *
     * @param eventId         parent event id
     * @param successListener callback to receive mapped data
     * @param snapshot        Firestore snapshot
     * @return mapped list (never {@code null})
     */
    private List<Image> dispatchImages(@NonNull String eventId,
                                       @Nullable OnSuccessListener<List<Image>> successListener,
                                       QuerySnapshot snapshot) {
        List<Image> images = mapImages(snapshot, eventId);
        if (successListener != null) {
            successListener.onSuccess(images);
        }
        return images;
    }

    /**
     * Converts a snapshot into {@link Image} models.
     *
     * @param snapshot query result
     * @return list of decoded images
     */
    private List<Image> mapImages(@Nullable QuerySnapshot snapshot, @NonNull String eventId) {
        List<Image> images = new ArrayList<>();
        if (snapshot == null) {
            return images;
        }
        snapshot.getDocuments().forEach(document -> {
            Image image = Image.fromSnapshot(document);
            if (image.getEventId() == null) {
                image.setEventId(eventId);
            }
            images.add(image);
        });
        return images;
    }

    @Nullable
    private String sanitizeId(@Nullable String candidate) {
        if (candidate == null) {
            return null;
        }
        String trimmed = candidate.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
