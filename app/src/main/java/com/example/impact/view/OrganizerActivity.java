package com.example.impact.view;

import androidx.fragment.app.Fragment;

import androidx.annotation.NonNull;

import com.example.impact.R;
import com.example.impact.utils.AppSession;

/**
 * Host activity for organizer workflows (event list, creation, notifications).
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
        return OrganizerEventListFragment.newInstance(organizerId);
    }

    @Override
    protected int getInitialToolbarTitle() {
        return R.string.admin_nav_events_tab; // This is not a mistake I am just lazy
    }

    @Override
    protected int getBottomNavigationMenuResource() {
        return R.menu.organizer_nav_menu;
    }

    @Override
    public Fragment getSelectedFragment(@NonNull int itemId) {
        if (itemId == R.id.organizer_nav_events) {
            return OrganizerEventListFragment.newInstance(organizerId);
        } else if (itemId == R.id.organizer_nav_create_event) {
            return OrganizerCreateEventFragment.newInstance(organizerId);
        } else if (itemId == R.id.organizer_nav_notifications) {
            return OrganizerNotificationsFragment.newInstance(organizerId); // This doesn't exist yet but it will
//            return OrganizerEventListFragment.newInstance(organizerId); // For now just return event view
        }
        return null;
    }

    @Override
    public int getSelectedToolBarTitle(@NonNull int itemId) {
        if (itemId == R.id.organizer_nav_events) {
            return R.string.admin_nav_events_tab; // This is not a mistake I am just lazy
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
