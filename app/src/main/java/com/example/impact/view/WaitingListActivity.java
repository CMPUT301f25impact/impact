package com.example.impact.view;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.impact.R;
import com.example.impact.view.adapter.EntrantRow;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays a live list of entrants waiting for a specific event.
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
