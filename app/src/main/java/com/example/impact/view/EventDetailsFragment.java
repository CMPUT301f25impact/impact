package com.example.impact.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.impact.R;
import com.example.impact.controller.WaitingListController;
import com.example.impact.model.Event;
import com.google.android.gms.common.annotation.NonNullApi;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Displays event details and lets entrants manage their waiting list status.
 */
public class EventDetailsFragment extends Fragment {
    public static final String EXTRA_EVENT = "event";
    public static final String EXTRA_ENTRANT_ID = "entrant_id";

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());

    private WaitingListController waitingListController;
    private Event event;
    private String entrantId;

    private Button joinButton;
    private Button leaveButton;
    private TextView statusText;
    private String currentStatus;

    /**
     * Factory method to create a new instance of this fragment
     * using the provided Event and entrant ID as arguments.
     */
    public static EventDetailsFragment newInstance(Event event, String entrantId) {
        EventDetailsFragment fragment = new EventDetailsFragment();
        Bundle args = new Bundle();

        args.putSerializable(EXTRA_EVENT, event);
        args.putString(EXTRA_ENTRANT_ID, entrantId);

        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            event = (Event) args.getSerializable(EXTRA_EVENT);
            entrantId = args.getString(EXTRA_ENTRANT_ID);
        }

        if (event == null || entrantId == null || entrantId.trim().isEmpty()) {
            if (getContext() != null) {
                Toast.makeText(getContext(), R.string.event_details_error_missing_data, Toast.LENGTH_SHORT).show();
            }
            getParentFragmentManager().popBackStack();
            return;
        }

        waitingListController = new WaitingListController();

        TextView nameText = view.findViewById(R.id.textViewEventDetailName);
        TextView dateText = view.findViewById(R.id.textViewEventDetailDate);
        TextView descriptionText = view.findViewById(R.id.textViewEventDetailDescription);
        statusText = view.findViewById(R.id.textViewEventDetailStatus);
        joinButton = view.findViewById(R.id.buttonJoinWaitingList);
        leaveButton = view.findViewById(R.id.buttonLeaveWaitingList);

        nameText.setText(event.getName());
        dateText.setText(formatDateRange(event));
        descriptionText.setText(event.getDescription());
        currentStatus = getString(R.string.event_status_pending);
        updateStatusLabel();

        joinButton.setOnClickListener(v -> joinWaitingList());
        leaveButton.setOnClickListener(v -> leaveWaitingList());

        resolveCurrentStatus();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof AppCompatActivity) {
            AppCompatActivity hostActivity = (AppCompatActivity) getActivity();
            if (hostActivity.getSupportActionBar() != null) {
                hostActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

                if (event != null) {
                    hostActivity.getSupportActionBar().setTitle(event.getName());
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() instanceof AppCompatActivity) {
            AppCompatActivity hostActivity = (AppCompatActivity) getActivity();
            if (hostActivity.getSupportActionBar() != null) {
                hostActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                hostActivity.getSupportActionBar().setTitle(R.string.entrant_nav_events_tab);
            }
        }
    }

    /**
     * Consumes Firestore to determine if the entrant already joined this event.
     */
    private void resolveCurrentStatus() {
        waitingListController.fetchWaitingListEntry(event.getId(), entrantId, entry -> {
            if (entry != null) {
                currentStatus = entry.getStatus() != null ? entry.getStatus() : getString(R.string.event_status_pending);
                updateStatusLabel();
                setButtonsForJoinedState(true);
            } else {
                currentStatus = getString(R.string.event_status_pending);
                updateStatusLabel();
                setButtonsForJoinedState(false);
            }
        }, error -> Toast.makeText(requireContext(), R.string.event_details_join_error, Toast.LENGTH_SHORT).show());
    }

    /**
     * Calls the controller to join the waiting list.
     */
    private void joinWaitingList() {
        waitingListController.joinWaitingList(event.getId(), event.getName(), entrantId, unused -> {
            currentStatus = getString(R.string.event_status_pending);
            updateStatusLabel();
            setButtonsForJoinedState(true);
            Toast.makeText(requireContext(), R.string.event_details_join_success, Toast.LENGTH_SHORT).show();
        }, error -> Toast.makeText(requireContext(), R.string.event_details_join_error, Toast.LENGTH_SHORT).show());
    }

    /**
     * Removes the entrant from the waiting list.
     */
    private void leaveWaitingList() {
        waitingListController.leaveWaitingList(event.getId(), entrantId, unused -> {
            currentStatus = getString(R.string.event_status_pending);
            updateStatusLabel();
            setButtonsForJoinedState(false);
            Toast.makeText(requireContext(), R.string.event_details_leave_success, Toast.LENGTH_SHORT).show();
        }, error -> Toast.makeText(requireContext(), R.string.event_details_leave_error, Toast.LENGTH_SHORT).show());
    }

    /**
     * Enables/disables CTA buttons based on membership.
     */
    private void setButtonsForJoinedState(boolean joined) {
        joinButton.setEnabled(!joined);
        leaveButton.setEnabled(joined);
    }

    /**
     * Refreshes the textual status indicator.
     */
    private void updateStatusLabel() {
        statusText.setText(getString(R.string.event_details_status_label, currentStatus));
    }

    /**
     * Formats the event date window for the header.
     */
    private String formatDateRange(Event event) {
        Date start = event.getStartDate();
        Date end = event.getEndDate();
        if (start != null && end != null) {
            return dateFormat.format(start) + " - " + dateFormat.format(end);
        }
        if (start != null) {
            return dateFormat.format(start);
        }
        if (end != null) {
            return dateFormat.format(end);
        }
        return "";
    }
}
