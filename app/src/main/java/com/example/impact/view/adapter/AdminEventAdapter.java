package com.example.impact.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.impact.R;
import com.example.impact.model.Event;
import com.example.impact.utils.DateUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying a list of events in the admin dashboard
 */
public class AdminEventAdapter extends RecyclerView.Adapter<AdminEventAdapter.AdminEventViewHolder> {
    private final List<Event> events;
    private final DeleteListener deleteListener;

    public interface DeleteListener {
        void onDeleteEventClicked(int position, Event event);
    }
    public AdminEventAdapter(DeleteListener deleteListener) {
        this.events = new ArrayList<>();
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public AdminEventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_event, parent, false);
        return new AdminEventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminEventViewHolder holder, int position) {
        Event currentEvent = events.get(position);
        holder.bind(currentEvent, position);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    /**
     * Update adapter with new events
     * @param newEvents new events to load
     */
    public void setEvents(List<Event> newEvents) {
        events.clear();
        if (newEvents != null) {
            events.addAll(newEvents);
        }
        notifyDataSetChanged();
    }

    /**
     * ViewHolder wrapper class to hold the views for a single event item
     */
     class AdminEventViewHolder extends RecyclerView.ViewHolder {
        final TextView nameTextView;
        final TextView descriptionTextView;
        final TextView dateTextView;
        final Button deleteButton;

        AdminEventViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.admin_event_name);
            descriptionTextView = itemView.findViewById(R.id.admin_event_description);
            dateTextView = itemView.findViewById(R.id.admin_event_date);
            deleteButton = itemView.findViewById(R.id.admin_event_delete_button);
        }

        void bind(Event event, int position) {
            nameTextView.setText(event.getName());
            dateTextView.setText(DateUtil.formatDateRange(event));
            descriptionTextView.setText(event.getDescription());

            deleteButton.setOnClickListener(v -> {
                deleteListener.onDeleteEventClicked(position, event);
            });
        }
    }
}
