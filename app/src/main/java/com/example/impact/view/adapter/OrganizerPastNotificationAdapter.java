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
import com.example.impact.model.Notification;
import com.example.impact.model.User;
import com.example.impact.utils.DateUtil;

import java.util.ArrayList;
import java.util.List;

public class OrganizerPastNotificationAdapter extends RecyclerView.Adapter<OrganizerPastNotificationAdapter.OrganizerNotificationViewHolder> {
    private final List<Notification> notifications;

    public OrganizerPastNotificationAdapter() {
        this.notifications = new ArrayList<>();
    }

    @NonNull
    @Override
    public OrganizerNotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_organizer_notification, parent, false);
        return new OrganizerPastNotificationAdapter.OrganizerNotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrganizerPastNotificationAdapter.OrganizerNotificationViewHolder holder, int position) {
        Notification currentNotification = notifications.get(position);
        holder.bind(currentNotification);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    /**
     * Update adapter with new notifications
     * @param newNotifications new notifications to load
     */
    public void setNotifications(List<Notification> newNotifications) {
        notifications.clear();
        if (newNotifications != null) {
            notifications.addAll(newNotifications);
        }
        notifyDataSetChanged();
    }

    /**
     * ViewHolder wrapper class to hold the views for a single event item
     */
    class OrganizerNotificationViewHolder extends RecyclerView.ViewHolder {
        final TextView recipientsTextView;
        final TextView messageTextView;
        final TextView dateTextView;

        OrganizerNotificationViewHolder(View itemView) {
            super(itemView);
            recipientsTextView = itemView.findViewById(R.id.textViewPastNotificationRecipients);
            messageTextView = itemView.findViewById(R.id.textViewPastNotificationMessage);
            dateTextView = itemView.findViewById(R.id.textViewPastNotificationTimeStamp);
        }

        void bind(Notification notification) {
            ArrayList<User> recipients = notification.getRecipients();
            StringBuilder stringBuilder = new StringBuilder();
            for (User user : recipients) {
                if (stringBuilder.length() > 0) {
                    stringBuilder.append(", ");
                }
                stringBuilder.append(user.getEmail());
            }
            String strRecipients = stringBuilder.toString();
            recipientsTextView.setText(strRecipients);

            dateTextView.setText("Sent " + notification.getTime_stamp().toString());
            messageTextView.setText(notification.getMessage());
        }
    }


}
