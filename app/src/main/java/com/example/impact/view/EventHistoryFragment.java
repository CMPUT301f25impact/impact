package com.example.impact.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.impact.R;
import com.example.impact.controller.UserController;
import com.example.impact.model.EntrantHistoryItem;
import com.example.impact.view.adapter.EventHistoryAdapter;

import java.util.List;

/**
 * Shows the entrant's historical event participation details.
 */
public class EventHistoryFragment extends Fragment {
    public static final String EXTRA_ENTRANT_ID = "entrant_id";

    private UserController userController;
    private EventHistoryAdapter historyAdapter;
    private TextView emptyStateView;
    private String entrantId;

    // Factory method to create a new instance of the fragment
    public static EventHistoryFragment newInstance(String entrantId) {
        EventHistoryFragment fragment = new EventHistoryFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_ENTRANT_ID, entrantId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            entrantId = getArguments().getString(EXTRA_ENTRANT_ID);
        }

        userController = new UserController();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_history, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewEventHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        historyAdapter = new EventHistoryAdapter();
        recyclerView.setAdapter(historyAdapter);

        emptyStateView = view.findViewById(R.id.textViewHistoryEmpty);

        if (entrantId != null && !entrantId.trim().isEmpty()) {
            loadHistory();
        } else {
            emptyStateView.setVisibility(View.VISIBLE);
        }

        return view;
    }

    /**
     * Requests the entrant's waiting-list history.
     */
    private void loadHistory() {
        userController.getEntrantHistory(entrantId, this::onHistoryLoaded,
                error -> Toast.makeText(requireContext(), R.string.event_history_error_loading, Toast.LENGTH_SHORT).show());
    }

    /**
     * Updates the recycler view with the fetched history.
     */
    private void onHistoryLoaded(List<EntrantHistoryItem> historyItems) {
        historyAdapter.setItems(historyItems);
        emptyStateView.setVisibility(historyItems == null || historyItems.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
