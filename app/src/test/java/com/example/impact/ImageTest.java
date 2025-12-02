package com.example.impact;

import com.example.impact.model.Image;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Tests for {@link Image} data serialization utilities.
 */
public class ImageTest {

    /**
     * Verifies the constructor populates mime type, file name, and base64 payload.
     */
    @Test
    public void testImageConstructorSetsFields() {
        Image img = new Image("image/png", "pic.png", "base64data");

        assertEquals("image/png", img.getMimeType());
        assertEquals("pic.png", img.getFileName());
        assertEquals("base64data", img.getBase64Content());
    }

    /**
     * Confirms setter methods update mutable fields.
     */
    @Test
    public void testImageSettersWork() {
        Image img = new Image();
        img.setMimeType("image/jpg");
        img.setFileName("photo.jpg");
        img.setBase64Content("data123");

        assertEquals("image/jpg", img.getMimeType());
        assertEquals("photo.jpg", img.getFileName());
        assertEquals("data123", img.getBase64Content());
    }

    /**
     * Ensures decoding fails gracefully for invalid base64 strings.
     */
    @Test
    public void testInvalidBase64ReturnsNull() {
        Image img = new Image("image/png", "bad.png", "");
        assertNull(img.decodeBase64ToBitmap());
    }

    /**
     * Ensures decoding also returns null when the payload is absent.
     */
    @Test
    public void testNullBase64ReturnsNull() {
        Image img = new Image("image/png", "bad2.png", null);
        assertNull(img.decodeBase64ToBitmap());
    }
}
