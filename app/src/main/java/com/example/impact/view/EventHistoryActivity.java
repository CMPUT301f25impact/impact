package com.example.impact.view;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.impact.R;
import com.example.impact.controller.EntrantController;
import com.example.impact.model.EntrantHistoryItem;
import com.example.impact.view.adapter.EventHistoryAdapter;

import java.util.List;

/**
 * Shows the entrant's historical event participation details.
 */
public class EventHistoryActivity extends AppCompatActivity {
    public static final String EXTRA_ENTRANT_ID = "entrant_id";

    private EntrantController entrantController;
    private EventHistoryAdapter historyAdapter;
    private TextView emptyStateView;
    private String entrantId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_history);

        entrantId = getIntent().getStringExtra(EXTRA_ENTRANT_ID);
        if (entrantId == null || entrantId.trim().isEmpty()) {
            Toast.makeText(this, R.string.entrant_profile_error_missing_id, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        entrantController = new EntrantController();

        RecyclerView recyclerView = findViewById(R.id.recyclerViewEventHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        historyAdapter = new EventHistoryAdapter();
        recyclerView.setAdapter(historyAdapter);

        emptyStateView = findViewById(R.id.textViewHistoryEmpty);

        loadHistory();
    }

    /**
     * Requests the entrant's waiting-list history.
     */
    private void loadHistory() {
        entrantController.getEntrantHistory(entrantId, this::onHistoryLoaded,
                error -> Toast.makeText(this, R.string.event_history_error_loading, Toast.LENGTH_SHORT).show());
    }

    /**
     * Updates the recycler view with the fetched history.
     */
    private void onHistoryLoaded(List<EntrantHistoryItem> historyItems) {
        historyAdapter.setItems(historyItems);
        emptyStateView.setVisibility(historyItems == null || historyItems.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
