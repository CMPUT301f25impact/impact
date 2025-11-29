package com.example.impact.view;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.impact.R;
import com.example.impact.utils.AppSession;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Abstract base activity for all dashboard screens.
 * Provides consistent header with title, back button, logout, and custom actions.
 * Provides consistent footer with configurable bottom navigation tabs.
 */
public abstract class BaseDashboardActivity extends AppCompatActivity {

    protected MaterialToolbar toolbar;
    protected BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_dashboard);
        AppSession.setStartupIntent(getIntent());

        toolbar = findViewById(R.id.dashboard_toolbar);
        bottomNav = findViewById(R.id.dashboard_bottom_nav_view);

        setSupportActionBar(toolbar);
        setupToolbar();
        setupBottomNavigation();

        // Load initial fragment
        if (savedInstanceState == null) {
            Fragment initialFragment = getInitialFragment();
            if (initialFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.dashboard_fragment_container, initialFragment)
                        .commit();
            }
        }
    }

    /**
     * Configure the toolbar with title, back button, and custom buttons
     */
    private void setupToolbar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getInitialToolbarTitle());

            // Show back button if needed
            if (shouldShowBackButton()) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }

        // Setup custom action buttons
        setupCustomToolbarActions();
    }

    /**
     * Configure the bottom navigation menu
     */
    private void setupBottomNavigation() {
        int menuResId = getBottomNavigationMenuResource();
        if (menuResId != 0) {
            bottomNav.inflateMenu(menuResId);
            bottomNav.setOnItemSelectedListener(navListener);
        } else {
            // Hide bottom navigation if no menu provided
            bottomNav.setVisibility(View.GONE);
        }
    }

    /**
     * Set the Bottom nagivation item options
     * Returns something like:
     * EventListFragment.newInstance(entrantId);
     */
    public abstract Fragment getSelectedFragment(@NonNull int itemId);

    /**
     * Set the Bottom nagivation item toolBarTitle options
     */
    public abstract int getSelectedToolBarTitle(@NonNull int itemId);

    /**
     * Bottom navigation item selection listener
     */
    private final BottomNavigationView.OnItemSelectedListener navListener =
            new BottomNavigationView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int itemId = item.getItemId();
                    Fragment selectedFragment = getSelectedFragment(itemId);
                    int toolbarTitle = getSelectedToolBarTitle(itemId);

                    if (selectedFragment != null) {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.dashboard_fragment_container, selectedFragment)
                                .commit();
                        if (getSupportActionBar() != null && toolbarTitle != 0) {
                            getSupportActionBar().setTitle(toolbarTitle);
                        }
                        return true;
                    }
                    return false;
                }
            };

    /**
     * Override this to add additional menu items
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_session, menu);

        // Allow subclasses to add additional menu items
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            performLogout();
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack();
            } else {
                onBackPressed(); // What is this?
            }
            return true;
        }

        // Allow subclasses to handle custom menu items
        return onCustomOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    /**
     * Performs logout: clears device ID from Firestore and navigates to login
     */
    protected void performLogout() {
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        if (TextUtils.isEmpty(deviceId)) {
            navigateToLogin();
            return;
        }

        AppSession.db().collection("users")
                .whereEqualTo("deviceId", deviceId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        queryDocumentSnapshots.forEach(documentSnapshot ->
                                documentSnapshot.getReference().update("deviceId", null));
                    }
                    Toast.makeText(this, R.string.logout_success, Toast.LENGTH_SHORT).show();
                    navigateToLogin();
                })
                .addOnFailureListener(error -> {
                    Toast.makeText(this, R.string.logout_error, Toast.LENGTH_SHORT).show();
                    navigateToLogin();
                });
    }

    /**
     * Navigate to login screen and clear activity stack
     */
    protected void navigateToLogin() {
        AppSession.initialize(null);
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Helper method to update toolbar title
     */
    protected void setToolbarTitle(int titleResId) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(titleResId);
        }
    }

    /**
     * Helper method to update toolbar title
     */
    protected void setToolbarTitle(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    // ========== Abstract Methods - Must be implemented by subclasses ==========

    /**
     * @return The initial fragment to display when the activity is created
     */
    protected abstract Fragment getInitialFragment();

    /**
     * @return The initial toolbar title resource ID
     */
    protected abstract int getInitialToolbarTitle();

    /**
     * @return The menu resource ID for bottom navigation, or 0 to hide bottom nav
     */
    protected abstract int getBottomNavigationMenuResource();


    // ========== Optional Override Methods ==========

    /**
     * @return true to show back button in toolbar, false otherwise
     */
    protected boolean shouldShowBackButton() {
        return false;
    }

    /**
     * Setup custom action buttons in the toolbar
     * Override this to add custom buttons to the toolbar
     */
    protected void setupCustomToolbarActions() {
        // Default: no custom actions
    }

    /**
     * Add custom menu items to the options menu
     * @param menu The options menu
     */
    protected void onCreateCustomOptionsMenu(Menu menu) {
        // Default: no custom menu items
    }

    /**
     * Handle custom options menu item selection
     * @param item The selected menu item
     * @return true if handled, false otherwise
     */
    protected boolean onCustomOptionsItemSelected(MenuItem item) {
        return false;
    }
}