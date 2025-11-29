package com.example.impact.view;

import android.text.TextUtils;
import android.view.MenuItem;

import androidx.fragment.app.Fragment;

import androidx.annotation.NonNull;

import com.example.impact.R;
import com.example.impact.utils.AppSession;

/**
 * Launcher screen for entrant-specific tools and shortcuts.
 * Now extends BaseDashboardActivity for consistent UI/UX.
 */
public class EntrantActivity extends BaseDashboardActivity
        implements EntrantProfileFragment.ProfileInteractionListener {

    private static final String PLACEHOLDER_ENTRANT_ID = "demo-entrant";
    private String entrantId;

    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        // Initialize entrantId before calling super.onCreate()
        entrantId = AppSession.getUserId();
        if (TextUtils.isEmpty(entrantId)) {
            entrantId = PLACEHOLDER_ENTRANT_ID;
        }

        super.onCreate(savedInstanceState);
    }

    @Override
    protected Fragment getInitialFragment() {
        return EventListFragment.newInstance(entrantId);
    }

    @Override
    protected int getInitialToolbarTitle() {
        return R.string.entrant_nav_events_tab;
    }

    @Override
    protected int getBottomNavigationMenuResource() {
        return R.menu.entrant_nav_menu;
    }

    @Override
    public Fragment getSelectedFragment(@NonNull int itemId) {
        if (itemId == R.id.entrant_nav_events) {
            return EventListFragment.newInstance(entrantId);
//            toolbarTitle = R.string.entrant_nav_events_tab;
        } else if (itemId == R.id.entrant_nav_profile) {
            return EntrantProfileFragment.newInstance(entrantId);
//            toolbarTitle = R.string.entrant_nav_profile_tab;
        } else if (itemId == R.id.entrant_nav_history) {
            return EventHistoryFragment.newInstance(entrantId);
//            toolbarTitle = R.string.entrant_nav_history_tab;
        }
        return null;
    }

    @Override
    public int getSelectedToolBarTitle(@NonNull int itemId) {
        if (itemId == R.id.entrant_nav_events) {
            return R.string.entrant_nav_events_tab;
        } else if (itemId == R.id.entrant_nav_profile) {
            return R.string.entrant_nav_profile_tab;
        } else if (itemId == R.id.entrant_nav_history) {
            return R.string.entrant_nav_history_tab;
        }
        return getInitialToolbarTitle();
    }

    /**
     * Callback for when a user deletes their profile while signed in
     */
    @Override
    public void onProfileDeleted() {
        performLogout();
    }
}