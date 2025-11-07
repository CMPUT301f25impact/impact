package com.example.impact.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.impact.R;
import com.example.impact.model.Event;

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
    private final @Nullable String currentUserId;  // add
    /**
     * Creates an adapter for entrant consumption without organizer context.
     *
     * @param listener click listener for row interactions
     */
    public EventAdapter(OnEventClickListener listener) {
        this.listener = listener;
        this.currentUserId = null;
    }
    /**
     * Creates an adapter that can tailor behaviour based on the current user id.
     *
     * @param listener      click listener for row interactions
     * @param currentUserId optional identifier for behaviour toggles
     */
    public EventAdapter(OnEventClickListener listener, @Nullable String currentUserId) {
        this.listener = listener;
        this.currentUserId = currentUserId;
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

        /**
         * @param itemView inflated event row
         */
        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.textViewEventName);
            dateText = itemView.findViewById(R.id.textViewEventDate);
            descriptionText = itemView.findViewById(R.id.textViewEventDescription);
            btnViewEntrants= itemView.findViewById(R.id.btnViewEntrants); // <— NEW
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
            if (btnViewEntrants != null) {
                btnViewEntrants.setOnClickListener(v -> {
                    if (listener != null) listener.onViewEntrantsClicked(event);
                });
                btnViewEntrants.setFocusable(false);
                btnViewEntrants.setClickable(true);
            }
        }
    }
}
