package com.example.impact;

import static org.junit.Assert.*;

import com.example.impact.controller.ImageController;

import org.junit.Test;

public class ImageControllerTest {

    @Test
    public void testFetchAllImagesSuccess() {
        ImageController controller = new ImageController(null);

        controller.fetchAllImages(
                images -> assertNotNull(images),
                error -> fail("Should not fail")
        );
    }

    @Test
    public void testDeleteImageSuccess() {
        ImageController controller = new ImageController(null);

        controller.deleteImage(
                "IMG1",
                id -> assertEquals("IMG1", id),
                error -> fail("Should not fail")
        );
    }
}
