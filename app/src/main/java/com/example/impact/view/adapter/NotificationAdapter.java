package com.example.impact.view.adapter;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.impact.R;
import com.example.impact.model.Entrant;
import com.example.impact.model.Event;
import com.example.impact.model.Image;
import com.example.impact.model.Notification;
import com.example.impact.model.Organizer;
import com.example.impact.model.User;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Recycler adapter rendering the list of notifications available to entrants.
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    /** Interface to notify listeners when an notification is selected. */
    public interface OnNotificationClickListener {
        /**
         * Called when a list row is tapped.
         *
         * @param notification selected notification
         */
        void onNotificationClicked(Notification notification);
        /**
         * Called when the "View Entrants" CTA is pressed.
         *
         * @param notification notification to be displayed
         */
    }

    private List<Notification> notifications = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
    private final OnNotificationClickListener listener;

    private final @Nullable String currentUserRole;
    /**
     * Creates an adapter for entrant consumption without organizer context.
     *
     * @param listener click listener for row interactions
     */
    public NotificationAdapter(OnNotificationClickListener listener) {
        this.listener = listener;
        this.currentUserRole = Entrant.ROLE_KEY;
    }
    /**
     * Creates an adapter that can tailor behaviour based on the current user id.
     *
     * @param listener      click listener for row interactions
     * @param currentUserRole optional for type of EventAdapter
     */
    public NotificationAdapter(OnNotificationClickListener listener, @Nullable String currentUserRole) {
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
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    /**
     * Binds an notification to its row.
     *
     * @param holder   view holder
     * @param position adapter index being bound
     */
    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        holder.bind(notification);
    }

    /**
     * @return total number of notifications tracked by the adapter
     */
    @Override
    public int getItemCount() {
        return notifications.size();
    }

    /**
     * Replaces the adapter data with the provided values.
     *
     * @param notifications new notification list (if {@code null}, adapter becomes empty)
     */
    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications != null ? notifications : new ArrayList<>();
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
     * Holds references for an notification card row.
     */
    class NotificationViewHolder extends RecyclerView.ViewHolder {
        private final TextView senderText;
        private final TextView timeStampText;
        private final TextView recipientsText;
        private final TextView eventNameText;
        private final TextView messageText;

        /**
         * @param itemView inflated notification row
         */
        NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            senderText = itemView.findViewById(R.id.textViewSenderEmail);
            timeStampText = itemView.findViewById(R.id.textViewTimeStamp);
            recipientsText = itemView.findViewById(R.id.textViewRecipientsEmails);
            eventNameText = itemView.findViewById(R.id.textViewEventName);
            messageText = itemView.findViewById(R.id.textViewMessage);
        }

        /**
         * Binds an notification model to the row.
         *
         * @param notification notification rendered in this row
         */
        void bind(Notification notification) {

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


            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onNotificationClicked(notification);
                }
            });


            Log.d("NotificationAdapter", "bind event=" + notification.getId());

        }
    }

}
