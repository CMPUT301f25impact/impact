package com.example.impact.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.impact.R;
import com.example.impact.controller.NotificationController;
import com.example.impact.model.Entrant;
import com.example.impact.model.Notification;
import com.example.impact.utils.AppSession;
import com.example.impact.view.adapter.EntrantNotificationAdapter;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment showing notifications targeted at the signed-in entrant.
 */
public class EntrantNotificationsFragment extends Fragment {

    private static final String extraEntrantId = "extraEntrantId";

    private String entrantId;
    private NotificationController notificationController;
    private RecyclerView notificationsRecycler;
    private TextView noNotificationsText;
    private EntrantNotificationAdapter notificationAdapter;

    /**
     * Factory for bundling an entrant id argument with the fragment.
     *
     * @param entrantId entrant identifier
     * @return configured fragment instance
     */
    public static EntrantNotificationsFragment newInstance(String entrantId) {
        EntrantNotificationsFragment fragment = new EntrantNotificationsFragment();
        Bundle args = new Bundle();
        args.putString(extraEntrantId, entrantId);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Reads fragment arguments and initializes the notification controller.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            entrantId = getArguments().getString(extraEntrantId);
        }

        notificationController = new NotificationController();
    }

    /**
     * Inflates the entrant notification layout.
     */
    @Nullable
    /**
     * Wires up the recycler view after inflation.
     */
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_entrant_notifications, container, false);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        notificationsRecycler = view.findViewById(R.id.recyclerNotifications);
        noNotificationsText = view.findViewById(R.id.textNoNotifications);

        notificationsRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        notificationAdapter = new EntrantNotificationAdapter();
        notificationsRecycler.setAdapter(notificationAdapter);

        loadNotifications();
    }

    /**
     * Requests notifications for the current entrant and toggles the empty state.
     */
    private void loadNotifications() {
        if (entrantId == null || entrantId.trim().isEmpty()) {
            noNotificationsText.setText("No entrant id found.");
            noNotificationsText.setVisibility(View.VISIBLE);
            notificationsRecycler.setVisibility(View.GONE);
            return;
        }

        notificationController.getNotificationsForEntrant((Entrant) AppSession.getUser(),
                notifications -> {
                    onNotificationsLoaded(notifications);
                },
                error -> {
                    Toast.makeText(requireContext(), "Error when loading notifications", Toast.LENGTH_SHORT).show();
                });
    }
    
    /**
     * Updates UI state when notifications arrive.
     *
     * @param notifications notifications fetched for entrant
     */
    private void onNotificationsLoaded(List<Notification> notifications) {
        notificationAdapter.setNotifications(notifications);

        if (notifications.isEmpty()) {
            noNotificationsText.setVisibility(View.VISIBLE);
            notificationsRecycler.setVisibility(View.GONE);
        }
        else {
            noNotificationsText.setVisibility(View.GONE);
            notificationsRecycler.setVisibility(View.VISIBLE);
        }
    }
}
