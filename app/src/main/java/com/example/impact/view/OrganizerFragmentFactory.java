package com.example.impact.view;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.impact.R;

/**
 * Produces organizer dashboard fragments for a selected navigation tab.
 */
final class OrganizerFragmentFactory {

    private OrganizerFragmentFactory() {
        // Utility class
    }

    /**
     * Creates the fragment corresponding to the selected organizer menu item.
     *
     * @param itemId      selected bottom navigation identifier
     * @param organizerId optional organizer identifier needed by certain fragments
     * @return fragment configured for the tab or {@code null} if no mapping exists
     */
    @Nullable
    static Fragment createFragment(int itemId, @Nullable String organizerId) {
        if (itemId == R.id.organizer_nav_events) {
            return OrganizerEventListFragment.newInstance(organizerId);
        } else if (itemId == R.id.organizer_nav_create_event) {
            return OrganizerCreateEventFragment.newInstance(organizerId);
        } else if (itemId == R.id.organizer_nav_notifications) {
            return OrganizerNotificationsFragment.newInstance(organizerId);
        }
        return null;
    }
}
