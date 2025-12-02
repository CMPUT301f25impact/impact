package com.example.impact;

import com.example.impact.model.Image;

import org.junit.Test;
import static org.junit.Assert.*;

public class ImageModelBasicTest {

    @Test
    public void image_fields_setCorrectly() {
        Image img = new Image();

        img.setImageId("IMG123");
        img.setMimeType("image/png");
        img.setFileName("poster.png");
        img.setBase64Content("ABC123");   // mock string, not real content

        assertEquals("IMG123", img.getImageId());
        assertEquals("image/png", img.getMimeType());
        assertEquals("poster.png", img.getFileName());
        assertEquals("ABC123", img.getBase64Content());
    }

    @Test
    public void image_defaultConstructor_hasNullFields() {
        Image img = new Image();

        assertNull(img.getImageId());
        assertNull(img.getMimeType());
        assertNull(img.getFileName());
        assertNull(img.getBase64Content());
    }

    @Test
    public void image_constructor_setsFieldsCorrectly() {
        Image img = new Image("image/jpeg", "photo.jpg", "BASE64_DATA");

        assertEquals("image/jpeg", img.getMimeType());
        assertEquals("photo.jpg", img.getFileName());
        assertEquals("BASE64_DATA", img.getBase64Content());
    }
}
