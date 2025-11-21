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
 * Thin wrapper around role utilities for image-related operations.
 */
public class ImageController {

    /**
     * Retrieves images stored under the specified event.
     */
    public void fetchAllImages(@NonNull String eventId,
                               @Nullable OnSuccessListener<List<Image>> successListener,
                               @Nullable OnFailureListener failureListener) {
        attach(OrganizerDb.fetchEventImages(eventId), successListener, failureListener);
    }

    /**
     * Fetches a single event image (used for posters/QR codes).
     */
    public void fetchImage(@NonNull String eventId,
                           @NonNull String imageId,
                           @Nullable OnSuccessListener<Image> successListener,
                           @Nullable OnFailureListener failureListener) {
        attach(EntrantDb.fetchEventImage(eventId, imageId), successListener, failureListener);
    }

    /**
     * Creates or overwrites an event-scoped image document.
     */
    public void createImage(@NonNull String eventId,
                            @NonNull Image image,
                            @Nullable OnSuccessListener<String> successListener,
                            @Nullable OnFailureListener failureListener) {
        attach(OrganizerDb.uploadPoster(eventId, image), successListener, failureListener);
    }

    /**
     * Deletes an image nested under an event.
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
