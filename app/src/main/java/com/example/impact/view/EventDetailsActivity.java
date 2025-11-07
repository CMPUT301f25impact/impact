package com.example.impact.view;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.impact.R;
import com.example.impact.controller.WaitingListController;
import com.example.impact.model.Event;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Displays event details and lets entrants manage their waiting list status.
 */
public class EventDetailsActivity extends AppCompatActivity {
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        event = (Event) getIntent().getSerializableExtra(EXTRA_EVENT);
        entrantId = getIntent().getStringExtra(EXTRA_ENTRANT_ID);

        if (event == null || entrantId == null || entrantId.trim().isEmpty()) {
            Toast.makeText(this, R.string.event_details_error_missing_data, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        waitingListController = new WaitingListController();

        TextView nameText = findViewById(R.id.textViewEventDetailName);
        TextView dateText = findViewById(R.id.textViewEventDetailDate);
        TextView descriptionText = findViewById(R.id.textViewEventDetailDescription);
        statusText = findViewById(R.id.textViewEventDetailStatus);
        joinButton = findViewById(R.id.buttonJoinWaitingList);
        leaveButton = findViewById(R.id.buttonLeaveWaitingList);

        nameText.setText(event.getName());
        dateText.setText(formatDateRange(event));
        descriptionText.setText(event.getDescription());
        currentStatus = getString(R.string.event_status_pending);
        updateStatusLabel();

        joinButton.setOnClickListener(view -> joinWaitingList());
        leaveButton.setOnClickListener(view -> leaveWaitingList());

        resolveCurrentStatus();
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
        }, error -> Toast.makeText(this, R.string.event_details_join_error, Toast.LENGTH_SHORT).show());
    }

    /**
     * Calls the controller to join the waiting list.
     */
    private void joinWaitingList() {
        waitingListController.joinWaitingList(event.getId(), event.getName(), entrantId, unused -> {
            currentStatus = getString(R.string.event_status_pending);
            updateStatusLabel();
            setButtonsForJoinedState(true);
            Toast.makeText(this, R.string.event_details_join_success, Toast.LENGTH_SHORT).show();
        }, error -> Toast.makeText(this, R.string.event_details_join_error, Toast.LENGTH_SHORT).show());
    }

    /**
     * Removes the entrant from the waiting list.
     */
    private void leaveWaitingList() {
        waitingListController.leaveWaitingList(event.getId(), entrantId, unused -> {
            currentStatus = getString(R.string.event_status_pending);
            updateStatusLabel();
            setButtonsForJoinedState(false);
            Toast.makeText(this, R.string.event_details_leave_success, Toast.LENGTH_SHORT).show();
        }, error -> Toast.makeText(this, R.string.event_details_leave_error, Toast.LENGTH_SHORT).show());
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
