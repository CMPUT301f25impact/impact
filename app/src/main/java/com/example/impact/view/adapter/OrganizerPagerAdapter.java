package com.example.impact.view.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.impact.view.EntrantViewFragment;
import com.example.impact.view.OrganizerToolsFragment;

/**
 * Supplies organizer dashboard fragments to the ViewPager.
 */
public class OrganizerPagerAdapter extends FragmentStateAdapter {

    private static final int PAGE_COUNT = 2;

    public OrganizerPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new OrganizerToolsFragment();
        }
        return new EntrantViewFragment();
    }

    @Override
    public int getItemCount() {
        return PAGE_COUNT;
    }
}
