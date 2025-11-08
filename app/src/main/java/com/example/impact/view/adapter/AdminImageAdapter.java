package com.example.impact.view.adapter;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.impact.R;
import com.example.impact.model.Image;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for showing uploaded images to administrators.
 */
public class AdminImageAdapter extends RecyclerView.Adapter<AdminImageAdapter.AdminImageViewHolder> {
    private final List<Image> images;
    private final AdminImageAdapter.DeleteListener deleteListener;

    /**
     * Triggered when an admin taps the delete image button.
     */
    public interface DeleteListener {
        /**
         * Called when the delete action is triggered for an image row.
         *
         * @param position adapter position
         * @param image    image being removed
         */
        void onDeleteImageClicked(int position, Image image);
    }

    /**
     * Builds an adapter with the provided delete listener.
     *
     * @param deleteListener callback invoked upon delete taps
     */
    public AdminImageAdapter(AdminImageAdapter.DeleteListener deleteListener) {
        this.images = new ArrayList<>();
        this.deleteListener = deleteListener;
    }

    /**
     * Inflates an admin image row.
     *
     * @param parent parent recycler
     * @param viewType unused view type
     * @return view holder instance
     */
    @NonNull
    @Override
    public AdminImageAdapter.AdminImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_image, parent, false);
        return new AdminImageAdapter.AdminImageViewHolder(view);
    }

    /**
     * Binds an image to its row.
     *
     * @param holder   view holder
     * @param position adapter position
     */
    @Override
    public void onBindViewHolder(@NonNull AdminImageAdapter.AdminImageViewHolder holder, int position) {
        Image currentImage = images.get(position);
        holder.bind(currentImage, position);
    }

    /**
     * @return number of images rendered
     */
    @Override
    public int getItemCount() {
        return images.size();
    }

    /**
     * Updates the adapter dataset with the supplied images.
     *
     * @param newImages list of images to render (may be {@code null})
     */
    public void setImages(List<Image> newImages) {
        images.clear();
        if (newImages != null) {
            images.addAll(newImages);
        }
        notifyDataSetChanged();
    }

    /**
     * ViewHolder wrapper class to hold the views for a single image item.
     */
    class AdminImageViewHolder extends RecyclerView.ViewHolder {
        final TextView filenameTextView;
        final Button deleteButton;
        final ImageView imageView;

        /**
         * @param itemView inflated row view
         */
        AdminImageViewHolder(View itemView) {
            super(itemView);
            filenameTextView = itemView.findViewById(R.id.admin_image_filename);
            deleteButton = itemView.findViewById(R.id.admin_image_delete_button);
            imageView = itemView.findViewById(R.id.admin_image);
        }

        /**
         * Binds an image to the row.
         *
         * @param image    image being displayed
         * @param position adapter position
         */
        void bind(Image image, int position) {
            String imageFilename = image.getFileName();
            Bitmap imageBitmap = image.decodeBase64ToBitmap();

            if (imageFilename != null && !imageFilename.isEmpty()) {
                filenameTextView.setText(imageFilename);
            } else {
                filenameTextView.setText(R.string.admin_image_list_name_placeholder);
            }

            if (imageBitmap != null) {
                imageView.setImageBitmap(imageBitmap);
            } else {
                // Set a placeholder or error image if decoding fails
                imageView.setImageResource(android.R.drawable.ic_dialog_alert);
            }

            deleteButton.setOnClickListener(v -> {
                deleteListener.onDeleteImageClicked(position, image);
            });
        }
    }
}
