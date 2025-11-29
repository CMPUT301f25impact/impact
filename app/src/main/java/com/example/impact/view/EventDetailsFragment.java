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
    private Button acceptButton;
    private Button declineButton;
    private TextView countText;
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
        countText = view.findViewById(R.id.textViewEventDetailCount);
        TextView criteriaText = view.findViewById(R.id.textViewLotteryCriteria);
        if (criteriaText != null) {
            criteriaText.setText(R.string.event_details_lottery_criteria);
        }
        joinButton = view.findViewById(R.id.buttonJoinWaitingList);
        leaveButton = view.findViewById(R.id.buttonLeaveWaitingList);
        acceptButton = view.findViewById(R.id.buttonAcceptInvitation);
        declineButton = view.findViewById(R.id.buttonDeclineInvitation);

        nameText.setText(event.getName());
        dateText.setText(formatDateRange(event));
        descriptionText.setText(event.getDescription());
        currentStatus = getString(R.string.event_status_pending);
        updateStatusLabel();

        joinButton.setOnClickListener(v -> joinWaitingList());
        leaveButton.setOnClickListener(v -> leaveWaitingList());
        acceptButton.setOnClickListener(v -> acceptSelection());
        declineButton.setOnClickListener(v -> declineSelection());

        resolveCurrentStatus();
        loadWaitingListCount();
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
                updateInvitationButtons();
            } else {
                currentStatus = getString(R.string.event_status_pending);
                updateStatusLabel();
                setButtonsForJoinedState(false);
                updateInvitationButtons();
            }
        }, error -> Toast.makeText(requireContext(), R.string.event_details_join_error, Toast.LENGTH_SHORT).show());
    }

    /**
     * Loads the current number of entrants on the waiting list.
     */
    private void loadWaitingListCount() {
        waitingListController.fetchWaitingListCount(event.getId(), count -> {
            if (countText != null) {
                countText.setText(getString(R.string.event_details_waiting_list_count, count));
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
            updateInvitationButtons();
            Toast.makeText(requireContext(), R.string.event_details_join_success, Toast.LENGTH_SHORT).show();
        }, error -> Toast.makeText(requireContext(), R.string.event_details_join_error, Toast.LENGTH_SHORT).show());
    }

    /**
     * Removes the entrant from the waiting list or records a decline when they were selected.
     */
    private void leaveWaitingList() {
        waitingListController.leaveWaitingList(event.getId(), entrantId, unused -> {
            currentStatus = getString(R.string.event_status_pending);
            updateStatusLabel();
            setButtonsForJoinedState(false);
            updateInvitationButtons();
            Toast.makeText(requireContext(), R.string.event_details_leave_success, Toast.LENGTH_SHORT).show();
        }, error -> Toast.makeText(requireContext(), R.string.event_details_leave_error, Toast.LENGTH_SHORT).show());
    }

    /**
     * Marks the invitation as declined and lets the controller promote a replacement entrant.
     */
    private void declineSelection() {
        waitingListController.declineSelection(event.getId(), entrantId, unused -> {
            currentStatus = getString(R.string.event_status_not_selected);
            updateStatusLabel();
            setButtonsForJoinedState(false);
            updateInvitationButtons();
            Toast.makeText(requireContext(), R.string.event_details_decline_success, Toast.LENGTH_SHORT).show();
        }, error -> Toast.makeText(requireContext(), R.string.event_details_decline_error, Toast.LENGTH_SHORT).show());
    }

    /**
     * Enables/disables CTA buttons based on membership.
     */
    private void setButtonsForJoinedState(boolean joined) {
        joinButton.setEnabled(!joined);
        leaveButton.setEnabled(joined);
        updateInvitationButtons();
    }

    /**
     * Refreshes the textual status indicator.
     */
    private void updateStatusLabel() {
        statusText.setText(getString(R.string.event_details_status_label, currentStatus));
        updateInvitationButtons();
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

    /**
     * Enables or disables the accept invitation button based on the entrant status.
     */
    private void updateInvitationButtons() {
        if (acceptButton == null || declineButton == null) {
            return;
        }
        boolean isSelected = getString(R.string.event_status_selected).equalsIgnoreCase(currentStatus);
        acceptButton.setEnabled(isSelected);
        declineButton.setEnabled(isSelected);
        int visibility = isSelected ? View.VISIBLE : View.GONE;
        acceptButton.setVisibility(visibility);
        declineButton.setVisibility(visibility);
    }

    /**
     * Persists the entrant’s acceptance for their invitation.
     */
    private void acceptSelection() {
        waitingListController.acceptSelection(event.getId(), entrantId, unused -> {
            currentStatus = getString(R.string.event_status_accepted);
            updateStatusLabel();
            Toast.makeText(requireContext(), R.string.event_details_accept_success, Toast.LENGTH_SHORT).show();
        }, error -> Toast.makeText(requireContext(), R.string.event_details_accept_error, Toast.LENGTH_SHORT).show());
    }
}
