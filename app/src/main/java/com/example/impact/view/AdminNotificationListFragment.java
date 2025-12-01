package com.example.impact.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.impact.R;
import com.example.impact.controller.EventController;
import com.example.impact.controller.NotificationController;
import com.example.impact.model.Event;
import com.example.impact.model.Notification;
import com.example.impact.utils.DeletionConfirmationUtil;
import com.example.impact.view.adapter.AdminEventAdapter;
import com.example.impact.view.adapter.AdminNotificationAdapter;

import java.util.List;

/**
 * This is the list fragment that renders list of events in the admin dashboard.
 */
public class AdminNotificationListFragment extends Fragment
        implements AdminNotificationAdapter.DeleteListener {

    private AdminNotificationAdapter currentAdapter;

    private NotificationController notificationController;

    public static final String EXTRA_ADMIN_ID = "admin_id";

    // Use a static factory method to create the fragment and set arguments
    public static AdminNotificationListFragment newInstance(String adminId) {
        AdminNotificationListFragment fragment = new AdminNotificationListFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_ADMIN_ID, adminId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        notificationController = new NotificationController();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_list, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.admin_list_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        currentAdapter = new AdminNotificationAdapter(this);
        recyclerView.setAdapter(currentAdapter);

        loadNotifications();
        return view;
    }

    /**
     * Loads events using EventController
     */
    private void loadNotifications() {
        notificationController.fetchAvailableNotifications(notifications -> {
                    if (notifications.isEmpty()) {
                        Toast.makeText(requireContext(), "No notifications found", Toast.LENGTH_SHORT).show();
                    }
                    onNotificationsLoaded(notifications);
                },
                error -> Toast.makeText(getContext(), "Unable to load Notifications", Toast.LENGTH_SHORT).show());
    }

    /**
     * Callback when events are successfully loaded
     * @param notifications loaded events
     */
    private void onNotificationsLoaded(List<Notification> notifications) {
        currentAdapter.setNotifications(notifications);
    }

    /**
     * Callback when an event is successfully deleted
     */
    private void onNotificationDelete(String name) {
        String deleteText = getResources().getString(R.string.admin_event_list_deletion, name);
        Toast.makeText(getContext(), deleteText, Toast.LENGTH_SHORT).show();
        loadNotifications(); // reload events after deletion
    }

    @Override
    public void onDeleteNotificationClicked(int position, Notification notification) {
        String notificationId = notification.getId();
        String notificationMessage = notification.getMessage();

        DeletionConfirmationUtil confirmation = new DeletionConfirmationUtil(getContext(), "this notification",
                () -> {
                    notificationController.deleteNotification(notificationId, v -> onNotificationDelete(notificationMessage),
                            error -> Toast.makeText(getContext(), "Unable to delete Notification", Toast.LENGTH_SHORT).show());
                });
        confirmation.show();
    }
}
