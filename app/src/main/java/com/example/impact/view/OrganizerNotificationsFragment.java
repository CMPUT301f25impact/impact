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
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.impact.R;
import androidx.fragment.app.Fragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.impact.controller.EventController;
import com.example.impact.controller.NotificationController;
import com.example.impact.controller.UserController;
import com.example.impact.controller.WaitingListController;
import com.example.impact.model.Event;
import com.example.impact.model.Notification;
import com.example.impact.model.User;
import com.example.impact.model.WaitingListEntry;
import com.example.impact.utils.AppSession;
import com.google.firebase.firestore.FieldValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class OrganizerNotificationsFragment extends Fragment {

    private Spinner sEventSelect;
    private RadioGroup radioGroup;
    private RadioButton rbWinners, rbWaitingList, rbCancelled;
    private EditText etMessage;
    private Button btnSend;
    private Event selectedEvent;
    private List<User> globalUserWaitingList;
    private List<User> globalCancelledUsers;
    private List<User> globalUsersBeingNotified;

    private final NotificationController notificationController = new NotificationController();
    private final EventController eventController = new EventController();
    private final WaitingListController waitingListController = new WaitingListController();
    private final UserController userController = new UserController();
    public static final String EXTRA_ORGANIZER_ID = "organizer_id";

    public static OrganizerNotificationsFragment newInstance(String organizerId) {
        OrganizerNotificationsFragment fragment = new OrganizerNotificationsFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_ORGANIZER_ID, organizerId);
        fragment.setArguments(args);
        return fragment;
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

        btnSend.setEnabled(false); // Do not allow the user to send a notification until a event has been selected
        rbWinners.setEnabled(false);
        rbWaitingList.setEnabled(false);
        rbCancelled.setEnabled(false); // Do not allow the user to select a notification type until a event is selected.


        // Populate the spinner with event names
        eventController.fetchEventsByOrganizer(AppSession.getUserId(), events -> {
            if (events.isEmpty()) {
                Toast.makeText(requireContext(), "No events found", Toast.LENGTH_SHORT).show();
                sEventSelect.setEnabled(false);
                rbWinners.setEnabled(false);
                rbWaitingList.setEnabled(false);
                rbCancelled.setEnabled(false);
                etMessage.setEnabled(false);
                btnSend.setEnabled(false); // There are no events so this fragment is dead
                return;
            }
            ArrayAdapter<Event> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, events);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            sEventSelect.setAdapter(adapter);
            sEventSelect.setEnabled(true);
            sEventSelect.setOnItemSelectedListener(eventSelectListener(events));
        },
            exception -> {
            Toast.makeText(requireContext(), "Error loading events: " + exception.getMessage(),
                    Toast.LENGTH_SHORT).show();
        });
        // Need to set the listeners for the views
        // Now we have the  global variables set
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                globalUsersBeingNotified = new ArrayList<>();
                if (checkedId == R.id.rbWinners) {
                    for (User useri : globalUserWaitingList) {
                        if (useri.getStatus() == "selected") {
                            globalUsersBeingNotified.add(useri);
                        }
                    }
                }
                else if (checkedId == R.id.rbWaitingList) {
                    globalUsersBeingNotified = new ArrayList<>(globalUserWaitingList);
                }
                else { // rbCancelled
                    globalUsersBeingNotified = new ArrayList<>(globalCancelledUsers);
                }
                btnSend.setEnabled(true);
            }
        });

        btnSend.setOnClickListener(view -> createNotification());
        return v;
    }

    private void createNotification() {
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
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(requireContext(), "Notification sent successfully", Toast.LENGTH_SHORT).show();
                    // Do any other actions after successful save
                })
                .addOnFailureListener(error -> {
                    Toast.makeText(requireContext(), "Failed to send notification", Toast.LENGTH_SHORT).show();
                    Log.e("Notification", "Error creating notification: " + error.getMessage());
                });
    }

    private AdapterView.OnItemSelectedListener eventSelectListener(List<Event> events) {
        return new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                globalUserWaitingList = new ArrayList<>(); // Clear the waiting list if the user reselects
                globalCancelledUsers = new ArrayList<>(); // Clear the cancelled users in case the current user reselects.
                selectedEvent = events.get(position);
                // do something with the selected event

                // If there are entrants on the waiting list:
                waitingListController.fetchWaitingListByEventId(selectedEvent.getId(), waitingList -> {
                    if (waitingList.isEmpty()) {
                        Toast.makeText(requireContext(), "No entrants in waiting list found", Toast.LENGTH_SHORT).show();
                        sEventSelect.setEnabled(false);
                        rbWinners.setEnabled(false);
                        rbWaitingList.setEnabled(false);
                        rbCancelled.setEnabled(false);
                        etMessage.setEnabled(false);
                        btnSend.setEnabled(false); // There are no events so this fragment is dead
                        // Everything is already disabled ... just in case
                        return;
                    }

                    List<String> waitingListEntrantIds = new ArrayList<>();
                    for (WaitingListEntry entry : waitingList) {
                        waitingListEntrantIds.add(entry.getEntrantId());
                    }
                    List<String> queryOnRoles = Arrays.asList("entrant");
                    userController.fetchAllUsers(queryOnRoles, userList -> {
                        for (User useri : userList) {
                            if (waitingListEntrantIds.contains(useri.getId())) {
                                globalUserWaitingList.add(useri);
                            }
                        }
                        return;
                    },
                    exception -> {
                        Toast.makeText(requireContext(), "Error loading events: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                    });

                    rbWaitingList.setEnabled(true);

                    // Logic that checks if the event lottery has happened yet
                    if (selectedEvent.isLotteryDone()) {
                        // And if there are winners (meaning there were some entrants for the event)
                        // There will always be winners right?
                        rbWinners.setEnabled(true);

                        // If there are cancelled entrants
                        // Best way to get cancelled entrants is to compare the entrantIds from the waiting list here to ALL user ids.

                        for (User useri : globalUserWaitingList) {
                            if (useri.getStatus().equals("cancelled")) {
                                globalCancelledUsers.add(useri);
                            }
                        }
                        // Now we can check if the cancelled users list is populated
                        if (!globalCancelledUsers.isEmpty()) {
                            rbCancelled.setEnabled(true);
                        }
                        return;
                    }
                    return;
                },
                exception -> {
                    Toast.makeText(requireContext(), "Error loading events: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                });







            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // We don't need to do anything if there is nothing selected.
            }
        };
    }


}
