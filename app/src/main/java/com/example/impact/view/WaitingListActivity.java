package com.example.impact.view;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import com.example.impact.R;
import com.example.impact.controller.WaitingListController;
import com.example.impact.model.Organizer;
import com.example.impact.utils.AppSession;
import com.example.impact.view.adapter.EntrantRow;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays organizer-specific waiting list information grouped by entrant status.
 */
public class WaitingListActivity extends AppCompatActivity {

    private static final String STATUS_PENDING = "pending";
    private static final String STATUS_SELECTED = "selected";
    private static final String STATUS_CANCELLED = "cancelled";
    private static final String STATUS_ACCEPTED = "accepted";

    private SimpleEntrantAdapter pendingAdapter;
    private SimpleEntrantAdapter selectedAdapter;
    private SimpleEntrantAdapter cancelledAdapter;
    private SimpleEntrantAdapter acceptedAdapter;
    private int pendingCount = 0;
    private int selectedCount = 0;
    private int acceptedCount = 0;
    private int cancelledCount = 0;
    private TextView pendingEmptyView;
    private TextView selectedEmptyView;
    private TextView cancelledEmptyView;
    private TextView acceptedEmptyView;
    private TextView lotteryCriteriaView;
    private Button runLotteryButton;
    private Button redrawButton;
    private ListenerRegistration pendingRegistration;
    private ListenerRegistration selectedRegistration;
    private ListenerRegistration cancelledRegistration;
    private ListenerRegistration acceptedRegistration;

    private WaitingListController waitingListController;
    private String eventId;
    private String eventName;
    @Nullable
    private Integer eventCapacity;
    private boolean lotteryAlreadyRun;
    private boolean isOrganizer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_list);
        Toolbar toolbar = findViewById(R.id.waiting_list_toolbar);
        setSupportActionBar(toolbar);

        waitingListController = new WaitingListController();
        isOrganizer = Organizer.ROLE_KEY.equals(AppSession.getRole());

        runLotteryButton = findViewById(R.id.buttonRunLottery);
        redrawButton = findViewById(R.id.buttonRedraw);
        redrawButton.setOnClickListener(v -> redrawLottery());
        pendingEmptyView = findViewById(R.id.textViewPendingEmpty);
        selectedEmptyView = findViewById(R.id.textViewSelectedEmpty);
        cancelledEmptyView = findViewById(R.id.textViewCancelledEmpty);
        acceptedEmptyView = findViewById(R.id.textViewAcceptedEmpty);
        lotteryCriteriaView = findViewById(R.id.textViewLotteryCriteria);

        pendingAdapter = new SimpleEntrantAdapter();
        selectedAdapter = new SimpleEntrantAdapter();
        cancelledAdapter = new SimpleEntrantAdapter();
        acceptedAdapter = new SimpleEntrantAdapter();

        setupRecycler(R.id.recyclerPendingEntrants, pendingAdapter);
        setupRecycler(R.id.recyclerSelectedEntrants, selectedAdapter);
        setupRecycler(R.id.recyclerCancelledEntrants, cancelledAdapter);
        setupRecycler(R.id.recyclerAcceptedEntrants, acceptedAdapter);

        eventId = getIntent().getStringExtra("eventId");
        eventName = getIntent().getStringExtra("eventName");
        int capacityExtra = getIntent().getIntExtra("eventCapacity", -1);
        eventCapacity = capacityExtra >= 0 ? capacityExtra : null;
        lotteryAlreadyRun = getIntent().getBooleanExtra("lotteryDone", false);

        if (eventId == null || eventId.trim().isEmpty()) {
            Toast.makeText(this, R.string.event_details_error_missing_data, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(eventName);
        }

        startStatusSubscriptions();
        configureRunLotteryButton();
    }

    private void setupRecycler(int recyclerId, SimpleEntrantAdapter adapter) {
        RecyclerView recyclerView = findViewById(recyclerId);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setAdapter(adapter);
    }

    private void configureRunLotteryButton() {
        if (!isOrganizer) {
            runLotteryButton.setVisibility(View.GONE);
            if (redrawButton != null) {
                redrawButton.setVisibility(View.GONE);
            }
            if (lotteryCriteriaView != null) {
                lotteryCriteriaView.setVisibility(View.GONE);
            }
            return;
        }
        runLotteryButton.setVisibility(View.VISIBLE);
        if (lotteryCriteriaView != null) {
            lotteryCriteriaView.setVisibility(View.VISIBLE);
        }
        runLotteryButton.setOnClickListener(v -> runLottery());
        if (redrawButton != null) {
            redrawButton.setOnClickListener(v -> redrawLottery());
        }

        determineLotteryRunState();
    }


    private void startStatusSubscriptions() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        pendingRegistration = subscribeToStatus(firestore, STATUS_PENDING, pendingAdapter, pendingEmptyView);
        selectedRegistration = subscribeToStatus(firestore, STATUS_SELECTED, selectedAdapter, selectedEmptyView);
        cancelledRegistration = subscribeToStatus(firestore, STATUS_CANCELLED, cancelledAdapter, cancelledEmptyView);
        acceptedRegistration = subscribeToStatus(firestore, STATUS_ACCEPTED, acceptedAdapter, acceptedEmptyView);
    }

    private ListenerRegistration subscribeToStatus(FirebaseFirestore firestore,
                                                   String status,
                                                   SimpleEntrantAdapter adapter,
                                                   TextView emptyView) {
        return firestore.collection("waitingLists")
                .document(eventId)
                .collection("entrants")
                .whereEqualTo("status", status)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null) {
                        return;
                    }
                    List<EntrantRow> rows = mapRows(snapshot);
                    adapter.submit(rows);
                    if (emptyView != null) {
                        emptyView.setVisibility(rows.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                    switch (status) {
                        case STATUS_PENDING:
                            pendingCount = rows.size();
                            break;
                        case STATUS_SELECTED:
                            selectedCount = rows.size();
                            break;
                        case STATUS_ACCEPTED:
                            acceptedCount = rows.size();
                            break;
                        case STATUS_CANCELLED:
                            cancelledCount = rows.size();
                            break;
                    }
                });
    }

    private List<EntrantRow> mapRows(QuerySnapshot snapshot) {
        List<EntrantRow> rows = new ArrayList<>();
        for (DocumentSnapshot document : snapshot.getDocuments()) {
            String entrantId = document.getString("entrantId");
            String status = document.getString("status");
            com.google.firebase.Timestamp timestamp = document.getTimestamp("timestamp");
            String joined = timestamp != null ? timestamp.toDate().toString() : "-";
            rows.add(new EntrantRow(
                    entrantId != null ? entrantId : "-",
                    status != null ? status : STATUS_PENDING,
                    joined));
        }
        return rows;
    }

    private void determineLotteryRunState() {
        waitingListController.hasLotteryRun(eventId, alreadyRun -> {
            lotteryAlreadyRun = Boolean.TRUE.equals(alreadyRun);
            updateRunLotteryButton();
        }, error -> updateRunLotteryButton());
    }

    private void updateRunLotteryButton() {
        if (!isOrganizer) {
            runLotteryButton.setVisibility(View.GONE);
            if (redrawButton != null) {
                redrawButton.setVisibility(View.GONE);
            }
            if (lotteryCriteriaView != null) {
                lotteryCriteriaView.setVisibility(View.GONE);
            }
            return;
        }
        // Run Lottery can only be used before the lottery has run
        runLotteryButton.setVisibility(View.VISIBLE);
        runLotteryButton.setEnabled(!lotteryAlreadyRun);
        // Redraw is only meaningful AFTER the initial lottery
        if (redrawButton != null) {
            redrawButton.setVisibility(lotteryAlreadyRun ? View.VISIBLE : View.GONE);
            redrawButton.setEnabled(lotteryAlreadyRun);
        }
        if (lotteryCriteriaView != null) {
            lotteryCriteriaView.setVisibility(View.VISIBLE);
        }
    }


    private void updateRedrawAvailability() {
        if (!isOrganizer || redrawButton == null) return;

        // Only visible when lottery has already run
        if (!lotteryAlreadyRun) {
            redrawButton.setVisibility(View.GONE);
            return;
        }

        // Not enough info to validate capacity
        if (eventCapacity == null) {
            boolean canRedraw = pendingCount > 0;
            redrawButton.setVisibility(View.VISIBLE);
            redrawButton.setEnabled(canRedraw);
            redrawButton.setAlpha(canRedraw ? 1f : 0.5f);
            return;
        }

        // Full? No redraw.
        int filled = selectedCount + acceptedCount;
        boolean hasVacancy = filled < eventCapacity;
        boolean hasPending = pendingCount > 0;

        boolean canRedraw = hasPending && hasVacancy;

        redrawButton.setVisibility(View.VISIBLE);
        redrawButton.setEnabled(canRedraw);
        redrawButton.setAlpha(canRedraw ? 1f : 0.5f);
    }


    private void runLottery() {
        if (!isOrganizer) {
            return;
        }
        runLotteryButton.setEnabled(false);
        waitingListController.runLottery(eventId, eventCapacity, unused -> {
            lotteryAlreadyRun = true;
            updateRunLotteryButton();
            Toast.makeText(this, R.string.event_details_run_lottery_success, Toast.LENGTH_SHORT).show();
        }, error -> {
            lotteryAlreadyRun = false;
            updateRunLotteryButton();
            Toast.makeText(this, R.string.event_details_run_lottery_error, Toast.LENGTH_SHORT).show();
        });
    }

    private void redrawLottery() {
        if (!isOrganizer) {
            return;
        }
        // If we know the capacity, make sure there is at least one free spot
        if (eventCapacity != null) {
            int filled = selectedCount + acceptedCount;
            if (filled >= eventCapacity) {
                // No vacancies left
                Toast.makeText(
                        this,
                        getString(R.string.waiting_list_redraw_full_error),
                        Toast.LENGTH_SHORT
                ).show();
                updateRedrawAvailability();
                return;
            }
        }
        // Need at least one pending entrant to redraw
        if (pendingCount <= 0) {
            Toast.makeText(
                    this,
                    getString(R.string.waiting_list_redraw_no_pending_error),
                    Toast.LENGTH_SHORT
            ).show();
            updateRedrawAvailability();
            return;
        }
        waitingListController.redrawNextEntrant(eventId, unused -> {
            Toast.makeText(this, R.string.waiting_list_redraw_success, Toast.LENGTH_SHORT).show();
        }, error -> {
            Toast.makeText(this, R.string.waiting_list_redraw_error, Toast.LENGTH_SHORT).show();
        });
    }



    private void clearListeners() {
        if (pendingRegistration != null) {
            pendingRegistration.remove();
            pendingRegistration = null;
        }
        if (selectedRegistration != null) {
            selectedRegistration.remove();
            selectedRegistration = null;
        }
        if (cancelledRegistration != null) {
            cancelledRegistration.remove();
            cancelledRegistration = null;
        }
        if (acceptedRegistration != null) {
            acceptedRegistration.remove();
            acceptedRegistration = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearListeners();
    }
}
