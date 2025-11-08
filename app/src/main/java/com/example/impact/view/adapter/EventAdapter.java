package com.example.impact.view.adapter;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.impact.R;
import com.example.impact.controller.ImageController;
import com.example.impact.model.Image;
import com.example.impact.model.Entrant;
import com.example.impact.model.Event;
import com.example.impact.model.Organizer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Recycler adapter rendering the list of events available to entrants.
 */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    /** Interface to notify listeners when an event is selected. */
    public interface OnEventClickListener {
        /**
         * Called when a list row is tapped.
         *
         * @param event selected event
         */
        void onEventClicked(Event event);

        /**
         * Called when the "View Entrants" CTA is pressed.
         *
         * @param event event whose entrants should be displayed
         */
        void onViewEntrantsClicked(@NonNull Event event);
    }

    private List<Event> events = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
    private final OnEventClickListener listener;
    private final @Nullable String currentUserRole;  // add
    private final ImageController imageController = new ImageController();
    /**
     * Creates an adapter for entrant consumption without organizer context.
     *
     * @param listener click listener for row interactions
     */
    public EventAdapter(OnEventClickListener listener) {
        this.listener = listener;
        this.currentUserRole = Entrant.ROLE_KEY;
    }
    /**
     * Creates an adapter that can tailor behaviour based on the current user id.
     *
     * @param listener      click listener for row interactions
     * @param currentUserRole optional for type of EventAdapter
     */
    public EventAdapter(OnEventClickListener listener, @Nullable String currentUserRole) {
        this.listener = listener;
        this.currentUserRole = currentUserRole;
    }

    /**
     * Inflates an entrant-facing event row.
     *
     * @param parent parent recycler
     * @param viewType unused view type
     * @return view holder instance
     */
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    /**
     * Binds an event to its row.
     *
     * @param holder   view holder
     * @param position adapter index being bound
     */
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);
        holder.bind(event);
    }

    /**
     * @return total number of events tracked by the adapter
     */
    @Override
    public int getItemCount() {
        return events.size();
    }

    /**
     * Replaces the adapter data with the provided values.
     *
     * @param events new event list (if {@code null}, adapter becomes empty)
     */
    public void setEvents(List<Event> events) {
        this.events = events != null ? events : new ArrayList<>();
        notifyDataSetChanged();
    }


    /**
     * Formats the event start/end range for display.
     *
     * @param event event whose dates should be formatted
     * @return formatted date string (empty when no dates set)
     */
    private String formatDateRange(Event event) {
        Date start = event.getStartDate();
        Date end = event.getEndDate();
        if (start != null && end != null) {
            return dateFormat.format(start) + " - " + dateFormat.format(end);
        }
        if (start != null) {
            return dateFormat.format(start);
        }
        if (end != null) {
            return dateFormat.format(end);
        }
        return "";
    }

    /**
     * Holds references for an event card row.
     */
    class EventViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameText;
        private final TextView dateText;
        private final TextView descriptionText;
        private final View btnViewEntrants;
        private final ImageView ivPoster;


        /**
         * @param itemView inflated event row
         */
        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.textViewEventName);
            dateText = itemView.findViewById(R.id.textViewEventDate);
            descriptionText = itemView.findViewById(R.id.textViewEventDescription);
            btnViewEntrants= itemView.findViewById(R.id.btnViewEntrants); // <— NEW
            ivPoster = itemView.findViewById(R.id.ivPoster);
            // if ivPoster is present, optionally set a placeholder so area is visible
            if (ivPoster != null) {
                ivPoster.setImageResource(android.R.drawable.ic_menu_report_image);
                ivPoster.setVisibility(View.VISIBLE);
            }
        }

        /**
         * Binds an event model to the row.
         *
         * @param event event rendered in this row
         */
        void bind(Event event) {
            nameText.setText(event.getName());
            dateText.setText(formatDateRange(event));
            descriptionText.setText(event.getDescription());

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEventClicked(event);
                }
            });
            // “View Entrants” button (for organizer)
            if (btnViewEntrants != null
                    && currentUserRole != null
                    && currentUserRole.equals(Organizer.ROLE_KEY)) {
                btnViewEntrants.setOnClickListener(v -> {
                    if (listener != null) listener.onViewEntrantsClicked(event);
                });
                btnViewEntrants.setFocusable(false);
                btnViewEntrants.setClickable(true);
            } else {
                if (btnViewEntrants == null) return;
                btnViewEntrants.setVisibility(View.GONE);
            }
            if (ivPoster == null) {
                return;
            }

            // Reset to placeholder first (prevents showing recycled bitmaps briefly)
            ivPoster.setImageResource(android.R.drawable.ic_menu_report_image);
            ivPoster.setVisibility(View.VISIBLE);

            String posterId = event.getPosterUrl(); // ensure Event has this getter
            Log.d("EventAdapter", "bind event=" + event.getId() + " posterId=" + posterId);

            if (posterId == null || posterId.trim().isEmpty()) {
                // No poster for this event - keep placeholder or hide if you prefer:
                // ivPoster.setVisibility(View.GONE);
                return;
            }
            // Fetch the image doc from "images" collection and load it
            imageController.fetchImage(posterId, new com.google.android.gms.tasks.OnSuccessListener<Image>() {
                @Override
                public void onSuccess(Image img) {
                    try {
                        if (img == null) {
                            return;
                        }
                        final Bitmap bmp = img.decodeBase64ToBitmap();
                        if (bmp == null) {
                            Log.w("EventAdapter", "decodeBase64ToBitmap returned null for id=" + posterId);
                            return;
                        }
                        // UI update on main thread
                        ivPoster.post(() -> {
                            ivPoster.setImageBitmap(bmp);
                            ivPoster.setVisibility(View.VISIBLE);
                        });
                        Log.d("EventAdapter", "Loaded poster for event=" + event.getId() + " posterId=" + posterId);
                    } catch (Exception ex) {
                        Log.e("EventAdapter", "Error while handling fetched image for id=" + posterId, ex);
                    }
                }
            }, new com.google.android.gms.tasks.OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("EventAdapter", "Failed to fetch image id=" + posterId, e);
                    // keep the placeholder (update on UI thread)
                    ivPoster.post(() -> ivPoster.setImageResource(android.R.drawable.ic_menu_report_image));
                }
            });

        }
    }
}
