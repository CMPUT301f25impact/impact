package com.example.impact.model;

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
     * Primary constructors
     * @param imageId unique ID to identify image
     * @param mimeType MIME image type
     * @param fileName file name
     * @param base64Content base64-encoded image data
     */
    public Image(String imageId, String mimeType, String fileName, String base64Content) {
        this.imageId = imageId;
        this.mimeType = mimeType;
        this.fileName = fileName;
        this.base64Content = base64Content;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getBase64Content() {
        return base64Content;
    }

    public void setBase64Content(String base64Content) {
        this.base64Content = base64Content;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Populates an image from a Firestore snapshot.
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
