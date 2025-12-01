package com.example.impact.view;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.impact.R;
import com.example.impact.controller.EventController;
import com.example.impact.controller.NotificationController;
import com.example.impact.controller.UserController;
import com.example.impact.controller.WaitingListController;
import com.example.impact.model.Event;
import com.example.impact.model.User;
import com.example.impact.model.WaitingListEntry;
import com.example.impact.model.Notification;
import com.example.impact.utils.AppSession;
import com.google.firebase.firestore.FieldValue;
import com.example.impact.view.adapter.NotificationAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OrganizerNotificationsFragment extends Fragment implements NotificationAdapter.OnNotificationClickListener {

    private Spinner sEventSelect;
    private RadioGroup radioGroup;
    private RadioButton rbWinners, rbWaitingList, rbCancelled;
    private EditText etMessage;
    private Button btnSend;
    private TextView emptyStateView;
    private Event selectedEvent;

    private List<Pair<User, WaitingListEntry>> globalWaitingListPairs;
    private List<User> globalUserWaitingList;
    private List<User> globalCancelledUsers;
    private List<User> globalSelectedUsers;
    private List<User> globalUsersBeingNotified;

    private final NotificationController notificationController = new NotificationController();
    private final EventController eventController = new EventController();
    private final WaitingListController waitingListController = new WaitingListController();
    private final UserController userController = new UserController();

    private NotificationAdapter notificationAdapter;
    public static final String EXTRA_ORGANIZER_ID = "organizer_id";

    public static OrganizerNotificationsFragment newInstance(String organizerId) {
        OrganizerNotificationsFragment fragment = new OrganizerNotificationsFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_ORGANIZER_ID, organizerId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        emptyStateView = view.findViewById(R.id.textViewEmptyState);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerNotifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        notificationAdapter = new NotificationAdapter(this);
        recyclerView.setAdapter(notificationAdapter);

        loadNotificatons();
    }
    /**
     * Requests events from Firestore, applying filters if needed.
     */
    private void loadNotificatons() {
        List<User> senderList = new ArrayList<>();
        senderList.add(AppSession.getUser());
        notificationController.fetchAllNotifications(senderList, this::onNotificationsLoaded,
                error -> Toast.makeText(requireContext(), R.string.event_list_error_loading, Toast.LENGTH_SHORT).show());

    }
    /**
     * Updates the adapter when Firestore returns results.
     */
    private void onNotificationsLoaded(List<Notification> notifications) {
        notificationAdapter.setNotifications(notifications);
        emptyStateView.setVisibility(notifications == null || notifications.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onNotificationClicked(Notification notification) {
        // This screen doesn't use. No-op is fine.
        // (Only Organizer screens navigate to WaitingListActivity.)
    }



    @SuppressLint({"CutPasteId", "MissingInflatedId"})
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_organizer_notifications, container, false);

        sEventSelect = v.findViewById(R.id.sEventSelect);
        radioGroup = v.findViewById(R.id.radioGroup);
        rbWinners = v.findViewById(R.id.rbWinners);
        rbWaitingList = v.findViewById(R.id.rbWaitingList);
        rbCancelled = v.findViewById(R.id.rbCancelled);
        etMessage = v.findViewById(R.id.etMessage);
        btnSend = v.findViewById(R.id.btnSend);

        btnSend.setEnabled(false);
        etMessage.setEnabled(true);
        rbWinners.setEnabled(false);
        rbWaitingList.setEnabled(false);
        rbCancelled.setEnabled(false);

        eventController.fetchEventsByOrganizer(AppSession.getUserId(), events -> {
            if (events.isEmpty()) {
                Toast.makeText(requireContext(), "No events found", Toast.LENGTH_SHORT).show();
                sEventSelect.setEnabled(false);
                rbWinners.setEnabled(false);
                rbWaitingList.setEnabled(false);
                rbCancelled.setEnabled(false);
                etMessage.setEnabled(false);
                btnSend.setEnabled(false);
                return;
            }
            ArrayAdapter<Event> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, events);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            sEventSelect.setAdapter(adapter);
            sEventSelect.setEnabled(true);
            sEventSelect.setOnItemSelectedListener(eventSelectListener(events));
        },
        exception -> Toast.makeText(requireContext(), "Error loading events: " + exception.getMessage(),
                Toast.LENGTH_SHORT).show());

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            globalUsersBeingNotified = new ArrayList<>();
            if (checkedId == R.id.rbWinners) {
                globalUsersBeingNotified.addAll(globalSelectedUsers);
            } else if (checkedId == R.id.rbWaitingList) {
                globalUsersBeingNotified.addAll(globalUserWaitingList);
            } else if (checkedId == R.id.rbCancelled) {
                globalUsersBeingNotified.addAll(globalCancelledUsers);
            }

            btnSend.setEnabled(!globalUsersBeingNotified.isEmpty());
        });

        btnSend.setOnClickListener(v1 -> sendNotification());

        return v;
    }

    private void sendNotification() {
        if (globalUsersBeingNotified == null || globalUsersBeingNotified.isEmpty()) {
            Toast.makeText(requireContext(), "No recipients selected", Toast.LENGTH_SHORT).show();
            return;
        }

        if (etMessage.getText().toString().trim().isEmpty()) {
            Toast.makeText(requireContext(), "Message cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        User sender = AppSession.getUser();
        ArrayList<User> recipients = new ArrayList<>(globalUsersBeingNotified);
        Event related_event = selectedEvent;
        String message = etMessage.getText().toString().trim();

        Map<String, Object> data = new HashMap<>();
        data.put("sender", sender);
        data.put("recipients", recipients);
        data.put("related_event", related_event);
        data.put("message", message);
        data.put("time_stamp", FieldValue.serverTimestamp());

        AppSession.db().collection("notifications")
                .add(data)
                .addOnSuccessListener(documentReference ->
                        Toast.makeText(requireContext(), "Notification sent successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(error -> {
                    Toast.makeText(requireContext(), "Failed to send notification", Toast.LENGTH_SHORT).show();
                    Log.e("Notification", "Error creating notification: " + error.getMessage());
                });
    }

    private AdapterView.OnItemSelectedListener eventSelectListener(List<Event> events) {
        return new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // We clear the global variables when a new event is selected
                globalWaitingListPairs = new ArrayList<>();
                globalUserWaitingList = new ArrayList<>();
                globalCancelledUsers = new ArrayList<>();
                globalSelectedUsers = new ArrayList<>();

                selectedEvent = events.get(position);

                waitingListController.fetchWaitingListByEventId(selectedEvent.getId(), waitingList -> {
                    if (waitingList.isEmpty()) {
                        Toast.makeText(requireContext(), "No entrants in waiting list found", Toast.LENGTH_SHORT).show();
                        rbWinners.setEnabled(false);
                        rbWaitingList.setEnabled(false);
                        rbCancelled.setEnabled(false);
                        etMessage.setEnabled(false);
                        btnSend.setEnabled(false);
                        return;
                    }
                    List<String> queryOnRoles = Arrays.asList("entrant");
                    userController.fetchAllUsers(queryOnRoles, userList -> {
                        Map<String, User> mapUsers = new HashMap<>();
                        for (User user : userList) {
                            mapUsers.put(user.getId(), user);
                        }
                        for (WaitingListEntry entry : waitingList) {
                            User user = mapUsers.get(entry.getEntrantId());
                            if (user != null) {
                                globalWaitingListPairs.add(new Pair<User, WaitingListEntry>(user, entry));
                            }
                        }
                        for (Pair<User, WaitingListEntry> pair : globalWaitingListPairs) {
                            if (pair.getEntry().getStatus().equals("selected")) {
                                globalSelectedUsers.add(pair.getUser());
                            }
                            else if (pair.getEntry().getStatus().equals("cancelled")) {
                                globalCancelledUsers.add(pair.getUser());
                            }
                            globalUserWaitingList.add(pair.getUser());
                        }
                        if (!globalUserWaitingList.isEmpty()) {
                            rbWaitingList.setEnabled(true);
                        }
                        if (!globalSelectedUsers.isEmpty()) {
                            rbWinners.setEnabled(true);
                        }
                        if (!globalCancelledUsers.isEmpty()) {
                            rbCancelled.setEnabled(true);
                        }
                    },
                    exception -> Toast.makeText(requireContext(), "Error loading events: " + exception.getMessage(), Toast.LENGTH_SHORT).show());
                },
                exception -> Toast.makeText(requireContext(), "Error loading events: " + exception.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No-op
            }
        };
    }

    private static class Pair<User, WaitingListEntry> {
        private final User user;
        private final WaitingListEntry entry;

        public Pair(User user, WaitingListEntry entry) {
            this.user = user;
            this.entry = entry;
        }
        public User getUser() {return user;}
        public WaitingListEntry getEntry() {return entry;}
    }
}
