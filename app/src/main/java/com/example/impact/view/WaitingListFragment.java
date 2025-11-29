package com.example.impact.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.impact.R;
import com.example.impact.model.Event;
import com.example.impact.view.adapter.EntrantRow;
import com.example.impact.view.adapter.SimpleEntrantAdapter;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays a live list of entrants waiting for a specific event.
 */
public class WaitingListFragment extends Fragment {

    // key in bundle for eventId
    public static final String ARG_EVENT = "event";

    private RecyclerView rv;
    private TextView noDataMessage;
    private SimpleEntrantAdapter adapter; // see step 2
    private Event event;
    private ListenerRegistration reg;

    /**
     * Factory method
     * @param event event instance
     * @return new instance
     */
    public static WaitingListFragment newInstance(Event event) {
        WaitingListFragment fragment = new WaitingListFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_EVENT, event);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            event = (Event) getArguments().getSerializable(ARG_EVENT);
        }

        // Handle missing event early
        if (event == null) {
            Toast.makeText(getContext(), "Missing event", Toast.LENGTH_SHORT).show();

            // remove fragment if no event id provided
            getParentFragmentManager().popBackStack();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_waiting_list, container, false);

        rv = view.findViewById(R.id.recyclerWaiting);
        noDataMessage = view.findViewById(R.id.waiting_list_no_data_text);

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new SimpleEntrantAdapter();
        rv.setAdapter(adapter);

        if (event != null) {
            loadWaitingListRealtime(event.getId());
        }

        return view;
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
     * Subscribes to waiting list updates for the provided event id.
     */
    private void loadWaitingListRealtime(String eventId) {
        reg = FirebaseFirestore.getInstance()
                .collection("waitingLists").document(eventId)
                .collection("entrants")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snap, e) -> {
                    if (e != null || snap == null) return;

                    List<EntrantRow> rows = new ArrayList<>();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        // Fields you actually save via WaitingListController
                        String entrantId = d.getString("entrantId");
                        String status = d.getString("status");
                        com.google.firebase.Timestamp ts = d.getTimestamp("timestamp");

                        String joined = (ts != null) ? ts.toDate().toString() : "-";

                        // If you want names/emails, you’ll need an extra fetch from /profiles/{entrantId}
                        rows.add(new EntrantRow(
                                entrantId,           // show id for now
                                status != null ? status : "pending",
                                joined));
                    }

                    if (rows.isEmpty()) {
                        rv.setVisibility(View.GONE);
                        noDataMessage.setVisibility(View.VISIBLE);
                    } else {
                        rv.setVisibility(View.VISIBLE);
                        noDataMessage.setVisibility(View.GONE);
                    }
                    adapter.submit(rows);
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (reg != null) {
            reg.remove();
            reg = null;
        }
    }
}
