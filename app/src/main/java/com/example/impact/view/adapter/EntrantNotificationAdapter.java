package com.example.impact.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.impact.R;
import com.example.impact.model.Notification;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter that displays entrant notifications only (offer messages, etc.)
 */
public class EntrantNotificationAdapter extends RecyclerView.Adapter<EntrantNotificationAdapter.NotificationViewHolder> {

    private List<Notification> notifications = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());

    public EntrantNotificationAdapter() {
        // no listener needed for entrants right now
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_entrant_notification, parent, false);

        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        holder.bind(notification);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications != null ? notifications : new ArrayList<>();
        notifyDataSetChanged();
    }

    class NotificationViewHolder extends RecyclerView.ViewHolder {

        private final TextView eventNameText;
        private final TextView messageText;
        private final TextView timeText;

        NotificationViewHolder(@NonNull View itemView) {
            super(itemView);

            eventNameText = itemView.findViewById(R.id.textEventName);
            messageText = itemView.findViewById(R.id.textMessage);
            timeText = itemView.findViewById(R.id.textTimeStamp);
        }

        void bind(Notification notification) {

            // Event name
            if (notification.getEventName() != null) {
                eventNameText.setText(notification.getEventName());
            } else {
                eventNameText.setText("Event");
            }

            // Message
            messageText.setText(notification.getMessage());

            // Timestamp
            Date timestamp = notification.getCreatedAt();
            if (timestamp != null) {
                timeText.setText(dateFormat.format(timestamp));
            } else {
                timeText.setText("");
            }
        }
    }
}
