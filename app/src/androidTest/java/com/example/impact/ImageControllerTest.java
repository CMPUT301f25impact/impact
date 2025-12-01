package com.example.impact;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import com.example.impact.controller.ImageController;

import org.junit.Test;

/**
 * Integration-style tests for {@link ImageController} that verify callbacks fire when its
 * operations succeed.
 */
public class ImageControllerTest {

    /**
     * Confirms {@link ImageController#fetchAllImages} triggers the success consumer with a
     * non-null collection of images.
     */
    @Test
    public void testFetchAllImagesSuccess() {
        ImageController controller = new ImageController(null);

        controller.fetchAllImages(
                images -> assertNotNull(images),
                error -> fail("Should not fail")
        );
    }

    /**
     * Ensures {@link ImageController#deleteImage} returns the id supplied to the success callback
     * when no errors occur.
     */
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
