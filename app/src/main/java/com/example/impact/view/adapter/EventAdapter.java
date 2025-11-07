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
        void onEventClicked(Event event);

        void onViewEntrantsClicked(@NonNull Event event);
    }

    private List<Event> events = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
    private final OnEventClickListener listener;
    private final @Nullable String currentUserId;  // add
    public EventAdapter(OnEventClickListener listener) {
        this.listener = listener;
        this.currentUserId = null;
    }
    public EventAdapter(OnEventClickListener listener, @Nullable String currentUserId) {
        this.listener = listener;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);
        holder.bind(event);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    /**
     * Replaces the adapter data with the provided values.
     */
    public void setEvents(List<Event> events) {
        this.events = events;
        notifyDataSetChanged();
    }


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

    class EventViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameText;
        private final TextView dateText;
        private final TextView descriptionText;
        private final View btnViewEntrants;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.textViewEventName);
            dateText = itemView.findViewById(R.id.textViewEventDate);
            descriptionText = itemView.findViewById(R.id.textViewEventDescription);
            btnViewEntrants= itemView.findViewById(R.id.btnViewEntrants); // <— NEW
        }

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
        }
    }
}}
