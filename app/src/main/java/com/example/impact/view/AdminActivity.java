package com.example.impact.view;

import android.text.TextUtils;
import android.view.MenuItem;

import androidx.fragment.app.Fragment;

import androidx.annotation.NonNull;

import com.example.impact.R;
import com.example.impact.utils.AppSession;

/**
 * Dashboard host activity for administrators, wiring up events, images, profiles, and notifications.
 */
public class AdminActivity extends BaseDashboardActivity {

    private String adminId;

    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        // Initialize entrantId before calling super.onCreate()
        adminId = AppSession.getUserId();

        super.onCreate(savedInstanceState);
    }

    @Override
    protected Fragment getInitialFragment() {
        return AdminEventListFragment.newInstance(adminId);
    }

    @Override
    protected int getInitialToolbarTitle() {
        return R.string.admin_nav_events_tab;
    }

    @Override
    protected int getBottomNavigationMenuResource() {
        return R.menu.admin_nav_menu;
    }

    @Override
    public Fragment getSelectedFragment(@NonNull int itemId) {
        if (itemId == R.id.admin_nav_events) {
            return AdminEventListFragment.newInstance(adminId);
        } else if (itemId == R.id.admin_nav_images) {
            return AdminImageListFragment.newInstance(adminId);
        } else if (itemId == R.id.admin_nav_profiles) {
            return AdminProfileListFragment.newInstance(adminId);
        }
        else if (itemId == R.id.admin_nav_notifications) {
            return AdminNotificationListFragment.newInstance(adminId);
        }
        return null;
    }

    @Override
    public int getSelectedToolBarTitle(@NonNull int itemId) {
        if (itemId == R.id.admin_nav_events) {
            return R.string.admin_nav_events_tab;
        } else if (itemId == R.id.admin_nav_profiles) {
            return R.string.admin_nav_profiles_tab;
        } else if (itemId == R.id.admin_nav_images) {
            return R.string.admin_nav_images_tab;
        }
        else if (itemId == R.id.admin_nav_notifications) {
            return R.string.admin_nav_notifications_tab;
        }
        return getInitialToolbarTitle();
    }

    @Override
    public int getActivityTitle() {
        return R.string.admin_dashboard_title;
    }

//    /**
//     * Callback for when a user deletes their profile while signed in
//     */
//    @Override
//    public void onProfileDeleted() {
//        performLogout();
//    }
}
