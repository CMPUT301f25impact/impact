package com.example.impact;

import com.example.impact.model.Image;

import org.junit.Test;

import static org.junit.Assert.*;

public class ImageTest {

    @Test
    public void testImageConstructorSetsFields() {
        Image img = new Image("image/png", "pic.png", "base64data");

        assertEquals("image/png", img.getMimeType());
        assertEquals("pic.png", img.getFileName());
        assertEquals("base64data", img.getBase64Content());
    }

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

    @Test
    public void testInvalidBase64ReturnsNull() {
        Image img = new Image("image/png", "bad.png", "");
        assertNull(img.decodeBase64ToBitmap());
    }

    @Test
    public void testNullBase64ReturnsNull() {
        Image img = new Image("image/png", "bad2.png", null);
        assertNull(img.decodeBase64ToBitmap());
    }
}
