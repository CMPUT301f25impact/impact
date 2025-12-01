package com.example.impact.view.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.impact.view.OrganizerCreateEventFragment;
import com.example.impact.view.OrganizerEventListFragment;

/**
 * Supplies organizer dashboard fragments to the ViewPager.
 */
public class OrganizerPagerAdapter extends FragmentStateAdapter {

    private static final int PAGE_COUNT = 2;
    private final String organizerId;

    /**
     * @param fragmentActivity host activity
     * @param organizerId organizer uid used to scope organizer data
     */
    public OrganizerPagerAdapter(@NonNull FragmentActivity fragmentActivity, String organizerId) {
        super(fragmentActivity);
        this.organizerId = organizerId;
    }
    // position 0 = EVENTS, position 1 = CREATE
    /**
     * Provides fragments for the requested page index.
     *
     * @param position pager index (0 = events, 1 = tools)
     * @return fragment configured for the requested tab
     */
    @Override @NonNull
    public Fragment createFragment(int position) {
        if (position == 0) {
            return OrganizerEventListFragment.newInstance(organizerId);
        }
        return OrganizerCreateEventFragment.newInstance(organizerId);
    }

    /**
     * @return fixed number of pages in the organizer dashboard
     */
    @Override
    public int getItemCount() {
        return PAGE_COUNT;
    }
}
