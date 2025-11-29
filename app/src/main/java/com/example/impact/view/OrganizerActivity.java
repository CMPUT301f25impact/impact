package com.example.impact.view;

import androidx.fragment.app.Fragment;

import androidx.annotation.NonNull;

import com.example.impact.R;
import com.example.impact.utils.AppSession;

/**
 * Launcher screen for entrant-specific tools and shortcuts.
 * Now extends BaseDashboardActivity for consistent UI/UX.
 */
public class OrganizerActivity extends BaseDashboardActivity {

    //    private static final String PLACEHOLDER_ENTRANT_ID = "demo-entrant";
    private String organizerId;

    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        // Initialize entrantId before calling super.onCreate()
         organizerId = AppSession.getUserId();

        super.onCreate(savedInstanceState);
    }

    @Override
    protected Fragment getInitialFragment() {
        return OrganizerFragmentFactory.createFragment(R.id.organizer_nav_events, organizerId);
    }

    @Override
    protected int getInitialToolbarTitle() {
        return R.string.organizer_nav_events_tab;
    }

    @Override
    protected int getBottomNavigationMenuResource() {
        return R.menu.organizer_nav_menu;
    }

    @Override
    public Fragment getSelectedFragment(@NonNull int itemId) {
        return OrganizerFragmentFactory.createFragment(itemId, organizerId);
    }

    @Override
    public int getSelectedToolBarTitle(@NonNull int itemId) {
        if (itemId == R.id.organizer_nav_events) {
            return R.string.organizer_nav_events_tab;
        } else if (itemId == R.id.organizer_nav_create_event) {
            return R.string.organizer_nav_create_event_tab;
        } else if (itemId == R.id.organizer_nav_notifications) {
            return R.string.organizer_nav_notifications_tab;
        }
        return getInitialToolbarTitle();
    }

    @Override
    public int getActivityTitle() {
        return R.string.organizer_dashboard_title;
    }

//    /**
//     * Callback for when a user deletes their profile while signed in
//     */
//    @Override
//    public void onProfileDeleted() {
//        performLogout();
//    }
}
