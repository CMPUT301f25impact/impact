package com.example.impact.view;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

    private TextView pendingEmptyView;
    private TextView selectedEmptyView;
    private TextView cancelledEmptyView;
    private TextView acceptedEmptyView;
    private Button runLotteryButton;

    private ListenerRegistration pendingRegistration;
    private ListenerRegistration selectedRegistration;
    private ListenerRegistration cancelledRegistration;
    private ListenerRegistration acceptedRegistration;

    private WaitingListController waitingListController;
    private String eventId;
    @Nullable
    private Integer eventCapacity;
    private boolean lotteryAlreadyRun;
    private boolean isOrganizer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_list);

        waitingListController = new WaitingListController();
        isOrganizer = Organizer.ROLE_KEY.equals(AppSession.getRole());

        runLotteryButton = findViewById(R.id.buttonRunLottery);
        pendingEmptyView = findViewById(R.id.textViewPendingEmpty);
        selectedEmptyView = findViewById(R.id.textViewSelectedEmpty);
        cancelledEmptyView = findViewById(R.id.textViewCancelledEmpty);
        acceptedEmptyView = findViewById(R.id.textViewAcceptedEmpty);

        pendingAdapter = new SimpleEntrantAdapter();
        selectedAdapter = new SimpleEntrantAdapter();
        cancelledAdapter = new SimpleEntrantAdapter();
        acceptedAdapter = new SimpleEntrantAdapter();

        setupRecycler(R.id.recyclerPendingEntrants, pendingAdapter);
        setupRecycler(R.id.recyclerSelectedEntrants, selectedAdapter);
        setupRecycler(R.id.recyclerCancelledEntrants, cancelledAdapter);
        setupRecycler(R.id.recyclerAcceptedEntrants, acceptedAdapter);

        eventId = getIntent().getStringExtra("eventId");
        int capacityExtra = getIntent().getIntExtra("eventCapacity", -1);
        eventCapacity = capacityExtra >= 0 ? capacityExtra : null;

        if (eventId == null || eventId.trim().isEmpty()) {
            Toast.makeText(this, R.string.event_details_error_missing_data, Toast.LENGTH_SHORT).show();
            finish();
            return;
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
            return;
        }
        runLotteryButton.setVisibility(View.VISIBLE);
        runLotteryButton.setOnClickListener(v -> runLottery());
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
            return;
        }
        runLotteryButton.setEnabled(!lotteryAlreadyRun);
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
