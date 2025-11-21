package com.example.impact.controller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.impact.model.Image;
import com.example.impact.utils.role.AdminDb;
import com.example.impact.utils.role.EntrantDb;
import com.example.impact.utils.role.OrganizerDb;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.List;

/**
 * Bridges UI callers to the image helpers provided by {@link OrganizerDb}, {@link EntrantDb}, and {@link AdminDb}.
 * No Firestore paths are constructed here—the controller simply forwards the request to the correct role utility.
 */
public class ImageController {

    /**
     * Retrieves every image in {@code events/{eventId}/images/} through {@link OrganizerDb}.
     *
     * @param eventId event whose images should be fetched
     * @param successListener optional callback with the loaded images
     * @param failureListener optional failure callback
     */
    public void fetchEventImages(@NonNull String eventId,
                                 @Nullable OnSuccessListener<List<Image>> successListener,
                                 @Nullable OnFailureListener failureListener) {
        attach(OrganizerDb.fetchEventImages(eventId), successListener, failureListener);
    }

    /**
     * Fetches a single image document owned by the event via {@link EntrantDb}.
     *
     * @param eventId event identifier
     * @param imageId document id inside {@code images/}
     * @param successListener optional success callback
     * @param failureListener optional failure callback
     */
    public void fetchImage(@NonNull String eventId,
                           @NonNull String imageId,
                           @Nullable OnSuccessListener<Image> successListener,
                           @Nullable OnFailureListener failureListener) {
        attach(EntrantDb.fetchEventImage(eventId, imageId), successListener, failureListener);
    }

    /**
     * Uploads or overwrites an image inside {@code events/{eventId}/images/} via {@link OrganizerDb#uploadPoster}.
     *
     * @param eventId event identifier
     * @param image model containing metadata and base64 content
     * @param successListener optional callback with the stored id
     * @param failureListener optional failure callback
     */
    public void uploadPoster(@NonNull String eventId,
                             @NonNull Image image,
                             @Nullable OnSuccessListener<String> successListener,
                             @Nullable OnFailureListener failureListener) {
        attach(OrganizerDb.uploadPoster(eventId, image), successListener, failureListener);
    }

    /**
     * Deletes an image nested under an event using {@link AdminDb}.
     *
     * @param eventId parent event id
     * @param imageId document id inside {@code images/}
     * @param successListener invoked with {@code imageId} on success
     * @param failureListener optional failure callback
     */
    public void deleteImage(@NonNull String eventId,
                            @NonNull String imageId,
                            @Nullable OnSuccessListener<String> successListener,
                            @Nullable OnFailureListener failureListener) {
        Task<Void> task = AdminDb.deleteEventImage(eventId, imageId);
        if (successListener != null) {
            task.addOnSuccessListener(unused -> successListener.onSuccess(imageId));
        }
        if (failureListener != null) {
            task.addOnFailureListener(failureListener);
        }
    }

    /**
     * Aggregates all images across every event for administrative browsing.
     *
     * @param successListener optional callback receiving the flattened image list
     * @param failureListener optional failure callback
     */
    public void fetchAllImagesAcrossEvents(@Nullable OnSuccessListener<List<Image>> successListener,
                                           @Nullable OnFailureListener failureListener) {
        attach(AdminDb.listAllImagesAcrossEvents(), successListener, failureListener);
    }

    /**
     * Applies optional listeners to a {@link Task}.
     */
    private <T> void attach(Task<T> task,
                            @Nullable OnSuccessListener<T> successListener,
                            @Nullable OnFailureListener failureListener) {
        if (successListener != null) {
            task.addOnSuccessListener(successListener);
        }
        if (failureListener != null) {
            task.addOnFailureListener(failureListener);
        }
    }
}
