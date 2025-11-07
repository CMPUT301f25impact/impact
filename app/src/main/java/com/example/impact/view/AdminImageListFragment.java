package com.example.impact.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.impact.R;
import com.example.impact.controller.ImageController;
import com.example.impact.model.Image;
import com.example.impact.utils.DeletionConfirmationUtil;
import com.example.impact.view.adapter.AdminImageAdapter;

import java.util.List;

/**
 * This is the list fragment that renders images in the admin dashboard
 */
public class AdminImageListFragment extends Fragment
        implements AdminImageAdapter.DeleteListener {

    private AdminImageAdapter adapter;

    private ImageController imageController;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imageController = new ImageController();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_list, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.admin_list_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new AdminImageAdapter(this);
        recyclerView.setAdapter(adapter);

        loadImages();
        return view;
    }

    /**
     * Loads images using ImageController
     */
    private void loadImages() {
        imageController.fetchAllImages(this::onImagesLoaded,
                error -> Toast.makeText(getContext(), R.string.event_list_error_loading, Toast.LENGTH_SHORT).show());
    }

    /**
     * Callback when images are successfully loaded
     * @param images loaded images
     */
    private void onImagesLoaded(List<Image> images) {
        adapter.setImages(images);
    }

    /**
     * Callback when an image is successfully deleted
     */
    private void onImageDelete(String name) {
        String deleteText = getResources().getString(R.string.admin_image_list_deletion, name);
        Toast.makeText(getContext(), deleteText, Toast.LENGTH_SHORT).show();
        loadImages(); // reload images after deletion
    }

    /**
     * Called when an image is deleted by clicking delete button
     * @param position image position in list
     * @param image image object
     */
    @Override
    public void onDeleteImageClicked(int position, Image image) {
        String imageId = image.getImageId();
        String imageFilename = image.getFileName();

        DeletionConfirmationUtil confirmation = new DeletionConfirmationUtil(getContext(), imageFilename,
                () -> {
                    imageController.deleteImage(imageId, v -> onImageDelete(imageFilename),
                            error -> Toast.makeText(getContext(), R.string.admin_image_list_error_deletion, Toast.LENGTH_SHORT).show());
                });

        confirmation.show();
    }

}
