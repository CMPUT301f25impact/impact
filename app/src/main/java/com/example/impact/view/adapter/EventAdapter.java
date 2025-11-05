package com.example.impact.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
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
    }

    private final List<Event> events = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
    private final OnEventClickListener listener;

    public EventAdapter(OnEventClickListener listener) {
        this.listener = listener;
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
    public void setEvents(List<Event> newEvents) {
        events.clear();
        if (newEvents != null) {
            events.addAll(newEvents);
        }
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

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.textViewEventName);
            dateText = itemView.findViewById(R.id.textViewEventDate);
            descriptionText = itemView.findViewById(R.id.textViewEventDescription);
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
        }
    }
}
