package com.example.impact.view;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.impact.R;
import com.example.impact.view.adapter.EntrantRow;
import com.example.impact.utils.role.OrganizerDb;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays a live list of entrants waiting for a specific event by listening to
 * {@link OrganizerDb#listenToWaitingList(String, com.google.firebase.firestore.EventListener)}.
 */
public class WaitingListActivity extends AppCompatActivity {

    private RecyclerView rv;
    private SimpleEntrantAdapter adapter; // see step 2

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_list);

        rv = findViewById(R.id.recyclerWaiting);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SimpleEntrantAdapter();
        rv.setAdapter(adapter);

        String eventId = getIntent().getStringExtra("eventId");
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Missing event id", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        loadWaitingListRealtime(eventId);
    }

    private ListenerRegistration reg;

    /**
     * Subscribes to waiting list updates for the provided event id.
     */
    private void loadWaitingListRealtime(String eventId) {
        reg = OrganizerDb.listenToWaitingList(eventId, (snap, e) -> {
            if (e != null || snap == null) return;

            List<EntrantRow> rows = new ArrayList<>();
            for (DocumentSnapshot d : snap.getDocuments()) {
                String entrantId = d.getString("entrantId");
                String status = d.getString("status");
                com.google.firebase.Timestamp ts = d.getTimestamp("timestamp");

                String joined = (ts != null) ? ts.toDate().toString() : "-";
                rows.add(new EntrantRow(
                        entrantId,
                        status != null ? status : "pending",
                        joined));
            }
            adapter.submit(rows);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (reg != null) {
            reg.remove();
            reg = null;
        }
    }
}
