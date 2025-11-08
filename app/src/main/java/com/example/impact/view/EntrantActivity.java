package com.example.impact.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.impact.R;
import com.example.impact.utils.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Launcher screen for entrant-specific tools and shortcuts.
 */
public class EntrantActivity extends AppCompatActivity implements EntrantProfileFragment.ProfileInteractionListener {
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
        BottomNavigationView bottomNav = findViewById(R.id.entrant_bottom_nav_view);
        bottomNav.setOnItemSelectedListener(navListener);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.entrant_nav_events_tab);
        }

        // on first load or if no saved state, redirect to default fragment
        if (savedInstanceState == null) {
            Fragment defaultFragment = EventListFragment.newInstance(entrantId);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.entrant_fragment_container, defaultFragment)
                    .commit();
        }
    }

    /**
     * Listener for bottom navigation menu
     */
    private final BottomNavigationView.OnItemSelectedListener navListener =
            new BottomNavigationView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;
                    int itemId = item.getItemId();

                    int toolbarTitle = R.string.entrant_dashboard_title;
                    if (itemId == R.id.entrant_nav_events) {
                        selectedFragment = EventListFragment.newInstance(entrantId);
                        toolbarTitle = R.string.entrant_nav_events_tab;
                    } else if (itemId == R.id.entrant_nav_profile) {
                        selectedFragment = EntrantProfileFragment.newInstance(entrantId);
                        toolbarTitle = R.string.entrant_nav_profile_tab;
                    } else if (itemId == R.id.entrant_nav_history) {
                        selectedFragment = EventHistoryFragment.newInstance(entrantId);
                        toolbarTitle = R.string.entrant_nav_history_tab;
                    }

                    // Replace the current fragment with the selected one
                    if (selectedFragment != null) {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.entrant_fragment_container, selectedFragment)
                                .commit();
                    }
                    if (getSupportActionBar() != null) {
                        getSupportActionBar().setTitle(toolbarTitle);
                    }
                    return true;
                }
            };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_session, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) { // if logout
            performLogout();
            return true;
        } else if (item.getItemId() == android.R.id.home) { // if back button
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }
    // Inside the Host Activity (e.g., MainActivity.java)

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
     * Callback for when a user deletes their profile while signed in
     */
    @Override
    public void onProfileDeleted() {
        performLogout();
    }
}
