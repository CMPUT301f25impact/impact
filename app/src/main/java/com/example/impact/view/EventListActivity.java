package com.example.impact.view;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.impact.R;
import com.example.impact.controller.EventController;
import com.example.impact.model.Event;
import com.example.impact.view.adapter.EventAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Displays a list of events and exposes filtering for entrant interests and availability.
 */
public class EventListActivity extends AppCompatActivity implements EventAdapter.OnEventClickListener {
    public static final String EXTRA_ENTRANT_ID = "entrant_id";

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());

    private EventController eventController;
    private EventAdapter eventAdapter;
    private TextView emptyStateView;
    private String entrantId;

    private List<String> selectedTags;
    @Nullable
    private Date selectedStartDate;
    @Nullable
    private Date selectedEndDate;
    @Override
    public void onViewEntrantsClicked(@NonNull Event event) {
        // This screen doesn’t use “View Entrants”. No-op is fine.
        // (Only Organizer screens navigate to WaitingListActivity.)
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);

        entrantId = getIntent().getStringExtra(EXTRA_ENTRANT_ID);

        eventController = new EventController();
        emptyStateView = findViewById(R.id.textViewEmptyState);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewEvents);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventAdapter = new EventAdapter(this);
        recyclerView.setAdapter(eventAdapter);

        Button filterButton = findViewById(R.id.buttonFilterEvents);
        Button clearFilterButton = findViewById(R.id.buttonClearFilter);

        filterButton.setOnClickListener(view -> showFilterDialog());
        clearFilterButton.setOnClickListener(view -> {
            selectedTags = null;
            selectedStartDate = null;
            selectedEndDate = null;
            loadEvents();
        });

        loadEvents();
    }

    private void loadEvents() {
        if (hasActiveFilter()) {
            eventController.fetchFilteredEvents(selectedTags, selectedStartDate, selectedEndDate, this::onEventsLoaded,
                    error -> Toast.makeText(this, R.string.event_list_error_loading, Toast.LENGTH_SHORT).show());
        } else {
            eventController.fetchAvailableEvents(this::onEventsLoaded,
                    error -> Toast.makeText(this, R.string.event_list_error_loading, Toast.LENGTH_SHORT).show());
        }
    }

    private boolean hasActiveFilter() {
        return (selectedTags != null && !selectedTags.isEmpty())
                || selectedStartDate != null
                || selectedEndDate != null;
    }

    private void onEventsLoaded(List<Event> events) {
        eventAdapter.setEvents(events);
        emptyStateView.setVisibility(events == null || events.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showFilterDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_event_filter, null, false);
        EditText interestInput = dialogView.findViewById(R.id.editTextFilterInterest);
        TextView startDateText = dialogView.findViewById(R.id.textViewFilterStartDate);
        TextView endDateText = dialogView.findViewById(R.id.textViewFilterEndDate);

        if (selectedTags != null && !selectedTags.isEmpty()) {
            interestInput.setText(TextUtils.join(", ", selectedTags));
        }
        final Date[] startHolder = new Date[]{selectedStartDate};
        final Date[] endHolder = new Date[]{selectedEndDate};
        updateDateLabel(startDateText, startHolder[0]);
        updateDateLabel(endDateText, endHolder[0]);

        startDateText.setOnClickListener(v -> showDatePicker(startHolder[0], date -> {
            startHolder[0] = date;
            updateDateLabel(startDateText, date);
        }));
        startDateText.setOnLongClickListener(v -> {
            startHolder[0] = null;
            updateDateLabel(startDateText, null);
            return true;
        });

        endDateText.setOnClickListener(v -> showDatePicker(endHolder[0], date -> {
            endHolder[0] = date;
            updateDateLabel(endDateText, date);
        }));
        endDateText.setOnLongClickListener(v -> {
            endHolder[0] = null;
            updateDateLabel(endDateText, null);
            return true;
        });

        new AlertDialog.Builder(this)
                .setTitle(R.string.event_filter_title)
                .setView(dialogView)
                .setPositiveButton(R.string.event_filter_apply, (dialog, which) -> {
                    selectedTags = parseTags(interestInput.getText().toString());
                    selectedStartDate = startHolder[0];
                    selectedEndDate = endHolder[0];
                    loadEvents();
                })
                .setNegativeButton(R.string.event_filter_cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private List<String> parseTags(String input) {
        if (TextUtils.isEmpty(input)) {
            return null;
        }
        String[] segments = input.split(",");
        List<String> tags = new ArrayList<>();
        for (String segment : segments) {
            String trimmed = segment.trim();
            if (!trimmed.isEmpty()) {
                tags.add(trimmed);
            }
        }
        return tags.isEmpty() ? null : tags;
    }

    private void updateDateLabel(TextView view, @Nullable Date date) {
        view.setText(date != null ? dateFormat.format(date) : getString(R.string.event_filter_any_date));
    }

    private void showDatePicker(@Nullable Date initialDate, DateSelectionCallback callback) {
        Calendar calendar = Calendar.getInstance();
        if (initialDate != null) {
            calendar.setTime(initialDate);
        }

        DatePickerDialog dialog = new DatePickerDialog(this, (picker, year, month, dayOfMonth) -> {
            Calendar selected = Calendar.getInstance();
            selected.set(Calendar.YEAR, year);
            selected.set(Calendar.MONTH, month);
            selected.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            selected.set(Calendar.HOUR_OF_DAY, 0);
            selected.set(Calendar.MINUTE, 0);
            selected.set(Calendar.SECOND, 0);
            selected.set(Calendar.MILLISECOND, 0);
            callback.onDateSelected(selected.getTime());
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        dialog.show();
    }

    @Override
    public void onEventClicked(Event event) {
        if (event == null) {
            return;
        }
        Intent intent = new Intent(this, EventDetailsActivity.class);
        intent.putExtra(EventDetailsActivity.EXTRA_EVENT, event);
        intent.putExtra(EventDetailsActivity.EXTRA_ENTRANT_ID, entrantId);
        startActivity(intent);
    }

    private interface DateSelectionCallback {
        void onDateSelected(Date date);
    }
}
