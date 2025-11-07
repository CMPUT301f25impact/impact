package com.example.impact.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.impact.R;
import com.example.impact.utils.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Launcher screen for entrant-specific tools and shortcuts.
 */
public class EntrantActivity extends AppCompatActivity {
    private static final String PLACEHOLDER_ENTRANT_ID = "demo-entrant";

    private String entrantId;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrant);

        entrantId = getIntent().getStringExtra(LoginActivity.EXTRA_USER_ID);
        if (TextUtils.isEmpty(entrantId)) {
            entrantId = PLACEHOLDER_ENTRANT_ID;
        }

        sessionManager = new SessionManager(this);

        MaterialToolbar toolbar = findViewById(R.id.entrantToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.entrant_dashboard_title);
        }

        Button profileButton = findViewById(R.id.buttonOpenProfile);
        Button eventsButton = findViewById(R.id.buttonOpenEvents);
        Button historyButton = findViewById(R.id.buttonOpenHistory);
        Button seedButton = findViewById(R.id.buttonSeedDemoData);

        profileButton.setOnClickListener(v -> startActivity(new Intent(this, EntrantProfileActivity.class)
                .putExtra(EntrantProfileActivity.EXTRA_ENTRANT_ID, entrantId)));

        eventsButton.setOnClickListener(v -> startActivity(new Intent(this, EventListActivity.class)
                .putExtra(EventListActivity.EXTRA_ENTRANT_ID, entrantId)));

        historyButton.setOnClickListener(v -> startActivity(new Intent(this, EventHistoryActivity.class)
                .putExtra(EventHistoryActivity.EXTRA_ENTRANT_ID, entrantId)));

        seedButton.setOnClickListener(v -> seedDemoEvents());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_session, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            performLogout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Clears session data then routes back to the login screen.
     */
    private void performLogout() {
        sessionManager.clearSession(() -> {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    /**
     * Writes a set of sample events to Firestore for demo purposes.
     */
    private void seedDemoEvents() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        WriteBatch batch = firestore.batch();

        List<Map<String, Object>> demoEvents = buildSampleEvents();
        for (Map<String, Object> eventData : demoEvents) {
            String eventId = (String) eventData.get("id");
            DocumentReference doc = firestore.collection("events").document(eventId);
            batch.set(doc, eventData);
        }

        batch.commit()
                .addOnSuccessListener(unused -> Toast.makeText(this, R.string.main_seed_demo_success, Toast.LENGTH_SHORT).show())
                .addOnFailureListener(error -> Toast.makeText(this, R.string.main_seed_demo_failure, Toast.LENGTH_SHORT).show());
    }

    /**
     * Builds the in-memory sample events used for seeding.
     */
    private List<Map<String, Object>> buildSampleEvents() {
        Calendar calendar = Calendar.getInstance();

        Map<String, Object> codingCamp = new HashMap<>();
        codingCamp.put("id", "demo-hackathon");
        codingCamp.put("name", "Impact Hackathon");
        codingCamp.put("description", "Build something meaningful in 24 hours with fellow entrants.");
        calendar.add(Calendar.DAY_OF_YEAR, 3);
        Date hackathonStart = calendar.getTime();
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        Date hackathonEnd = calendar.getTime();
        codingCamp.put("startDate", hackathonStart);
        codingCamp.put("endDate", hackathonEnd);
        codingCamp.put("posterUrl", null);
        codingCamp.put("tags", Arrays.asList("technology", "teamwork", "innovation"));

        Map<String, Object> yogaEvent = new HashMap<>();
        yogaEvent.put("id", "demo-wellness");
        yogaEvent.put("name", "Sunrise Wellness Retreat");
        yogaEvent.put("description", "Kick off your morning with guided yoga and mindfulness sessions.");
        calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 7);
        Date wellnessDay = calendar.getTime();
        yogaEvent.put("startDate", wellnessDay);
        yogaEvent.put("endDate", wellnessDay);
        yogaEvent.put("posterUrl", null);
        yogaEvent.put("tags", Arrays.asList("wellness", "outdoors", "community"));

        Map<String, Object> artFair = new HashMap<>();
        artFair.put("id", "demo-art");
        artFair.put("name", "Community Art Fair");
        artFair.put("description", "Showcase your creativity or learn from local artists in this weekend fair.");
        calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 14);
        Date artStart = calendar.getTime();
        calendar.add(Calendar.DAY_OF_YEAR, 2);
        artFair.put("startDate", artStart);
        artFair.put("endDate", calendar.getTime());
        artFair.put("posterUrl", null);
        artFair.put("tags", Arrays.asList("art", "community", "family"));

        return Arrays.asList(codingCamp, yogaEvent, artFair);
    }
}
