package com.example.impact.view;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.impact.R;
import com.example.impact.controller.NotificationController;
import com.example.impact.controller.UserController;
import com.example.impact.model.EntrantHistoryItem;
import com.example.impact.model.Organizer;
import com.example.impact.model.Notification;
import com.example.impact.model.User;
import com.example.impact.view.adapter.OrganizerPastNotificationAdapter;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class OrganizerSendNotification extends AppCompatActivity {
    private OrganizerPastNotificationAdapter pastNotificationAdapter;
    private TextView emptyStateView;
    private NotificationController notificatonController;
    private UserController userController;

    private Organizer current_organizer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_send_notification);

        Button sendButton = findViewById(R.id.send_button);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewPastNotifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        pastNotificationAdapter = new OrganizerPastNotificationAdapter();
        recyclerView.setAdapter(pastNotificationAdapter);

        emptyStateView = findViewById(R.id.textViewPastEmpty);
        notificatonController = new NotificationController();

        loadPast();
    }

    private void getCurrentOrganizer() {
        String organizerId = getIntent().getStringExtra("organizer_id");

        userController.fetchProfile(organizerId, this::onOrganizerFetch, error -> Toast.makeText(this, "Organizer not found", Toast.LENGTH_SHORT).show());
        if (current_organizer != null) {
            loadPast();
        } else {
            Toast.makeText(this, "Error: No organizer found", Toast.LENGTH_SHORT).show();
        }
    }

    private void onOrganizerFetch(User user) {
        current_organizer = (Organizer) user;
    }

    private void loadPast() {
        // Load all notifications
        notificatonController.fetchAllNotifications(List.of(current_organizer), this::onHistoryLoaded,
                error -> Toast.makeText(this, R.string.event_history_error_loading, Toast.LENGTH_SHORT).show());

    }

    /**
     * Updates the recycler view with the fetched history.
     */
    private void onHistoryLoaded(List<Notification> notifications) {
        pastNotificationAdapter.setNotifications(notifications);
        emptyStateView.setVisibility(notifications == null || notifications.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
