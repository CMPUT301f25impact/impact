package com.example.impact.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import androidx.annotation.Nullable;

import com.google.firebase.firestore.DocumentSnapshot;

import java.io.Serializable;

/**
 * Image model class for storing image information
 */
public class Image implements Serializable {

    private String imageId;
    private String base64Content;
    private String mimeType;
    private String fileName;

    /**
     * Required empty constructor for Firestore deserialization.
     */
    public Image() {
        // Firestore constructor
    }

    /**
     * Primary constructor.
     *
     * @param imageId        unique ID to identify image
     * @param mimeType       MIME image type
     * @param fileName       file name
     * @param base64Content  base64-encoded image data
     */
    public Image(String imageId, String mimeType, String fileName, String base64Content) {
        this.imageId = imageId;
        this.mimeType = mimeType;
        this.fileName = fileName;
        this.base64Content = base64Content;
    }

    /**
     * @return Firestore document id
     */
    public String getImageId() {
        return imageId;
    }

    /**
     * @param imageId Firestore document id
     */
    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    /**
     * @return base64 payload storing the image
     */
    public String getBase64Content() {
        return base64Content;
    }

    /**
     * @param base64Content updated base64 payload
     */
    public void setBase64Content(String base64Content) {
        this.base64Content = base64Content;
    }

    /**
     * @return MIME type (e.g., {@code image/png})
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * @param mimeType MIME type (e.g., {@code image/png})
     */
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    /**
     * @return original file name
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @param fileName original file name
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Attempts to decode stored base64 content to bitmap form.
     *
     * @return decoded bitmap or {@code null} when invalid
     */
    @Nullable
    public Bitmap decodeBase64ToBitmap() {
        try {
            if (base64Content == null || base64Content.isEmpty()) {
                return null;
            }

            byte[] decodedBytes = Base64.decode(base64Content, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) {
            return null;
        }
    }

    public static Bitmap decodeBase64ToBitmapStatic(String base64) {
        try {
            if (base64 == null || base64.isEmpty()) return null;
            byte[] decoded = android.util.Base64.decode(base64, android.util.Base64.DEFAULT);
            return android.graphics.BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
        } catch(Exception e) { return null; }
    }

    /**
     * Populates an image from a Firestore snapshot.
     *
     * @param snapshot Firestore document
     * @return mapped Image instance
     */
    public static Image fromSnapshot(DocumentSnapshot snapshot) {
        Image image = snapshot.toObject(Image.class);
        if (image == null) {
            image = new Image();
        }
        image.setImageId(snapshot.getId());
        return image;
    }
}
