package com.example.impact.view.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.impact.R;
import com.example.impact.model.Notification;
import com.example.impact.model.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Adapter for displaying a list of events in the admin dashboard
 */
public class AdminNotificationAdapter extends RecyclerView.Adapter<AdminNotificationAdapter.AdminNotificationViewHolder> {
    private final List<Notification> notifications;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
    private final DeleteListener deleteListener;

    /**
     * Triggered when an admin taps the delete button.
     */
    public interface DeleteListener {
        /**
         * Called when a row's delete action is triggered.
         *
         * @param position adapter position
         * @param notification  notification being deleted
         */
        void onDeleteNotificationClicked(int position, Notification notification);
    }
    /**
     * Builds an adapter with the provided deletion callback.
     *
     * @param deleteListener listener invoked when the delete button is tapped
     */
    public AdminNotificationAdapter(DeleteListener deleteListener) {
        this.notifications = new ArrayList<>();
        this.deleteListener = deleteListener;
    }

    /**
     * Inflates an admin event row.
     *
     * @param parent parent recycler view
     * @param viewType unused view type flag
     * @return populated view holder
     */
    @NonNull
    @Override
    public AdminNotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_notification, parent, false);
        return new AdminNotificationViewHolder(view);
    }

    /**
     * Binds an event to its row.
     *
     * @param holder   view holder
     * @param position adapter index being bound
     */
    @Override
    public void onBindViewHolder(@NonNull AdminNotificationViewHolder holder, int position) {
        Notification currentNotification = notifications.get(position);
        holder.bind(currentNotification, position);
    }

    /**
     * @return number of rendered events
     */
    @Override
    public int getItemCount() {
        return notifications.size();
    }

    /**
     * Updates the adapter dataset.
     *
     * @param newNotifications events to display (may be {@code null})
     */
    public void setNotifications(List<Notification> newNotifications) {
        notifications.clear();
        if (newNotifications != null) {
            notifications.addAll(newNotifications);
        }
        notifyDataSetChanged();
    }

    /**
     * Formats the  time stamp  for display.
     *
     * @param notification notification whose time stamp should be formatted
     * @return formatted date string (empty when no dates set)
     */
    private String formatDateRange(Notification notification) {
        Date time_stamp = notification.getTime_stamp();
        if (time_stamp != null) {
            return dateFormat.format(time_stamp);
        }
        return "";
    }

    /**
     * ViewHolder wrapper class to hold the views for a single event item.
     */
    class AdminNotificationViewHolder extends RecyclerView.ViewHolder {
        private final TextView senderText;
        private final TextView timeStampText;
        private final TextView recipientsText;
        private final TextView eventNameText;
        private final TextView messageText;
        final Button deleteButton;

        /**
         * @param itemView inflated row view
         */
        AdminNotificationViewHolder(View itemView) {
            super(itemView);
            senderText = itemView.findViewById(R.id.textViewSenderEmail);
            timeStampText = itemView.findViewById(R.id.textViewTimeStamp);
            recipientsText = itemView.findViewById(R.id.textViewRecipientsEmails);
            eventNameText = itemView.findViewById(R.id.textViewEventName);
            messageText = itemView.findViewById(R.id.textViewMessage);
            deleteButton = itemView.findViewById(R.id.admin_delete_notification_button);
        }

        /**
         * Binds event data to the row and wires up delete behavior.
         *
         * @param notification    notification being rendered
         */
        void bind(Notification notification, int position) {

            senderText.setText(notification.getSender().getEmail());
            timeStampText.setText(formatDateRange(notification));
            ArrayList<User> recipients = notification.getRecipients();
            recipientsText.setText(
                    recipients.stream()
                            .map(recip -> recip.getEmail())
                            .collect(Collectors.joining(", "))
            );
            if (notification.getRelated_event() != null) {
                eventNameText.setText(notification.getRelated_event().getName());
            }
            else {
                eventNameText.setText("");
            }
            messageText.setText(notification.getMessage());

            deleteButton.setOnClickListener(v -> {
                deleteListener.onDeleteNotificationClicked(position, notification);
            });


            Log.d("NotificationAdapter", "bind event=" + notification.getId());

        }
    }
}
