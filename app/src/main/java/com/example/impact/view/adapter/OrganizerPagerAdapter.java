package com.example.impact.view.adapter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.impact.view.EntrantViewFragment;
import com.example.impact.view.OrganizerEventsFragment;
import com.example.impact.view.OrganizerToolsFragment;

/**
 * Supplies organizer dashboard fragments to the ViewPager.
 */
public class OrganizerPagerAdapter extends FragmentStateAdapter {

    private static final int PAGE_COUNT = 2;
    private final String organizerEmail;

    public OrganizerPagerAdapter(@NonNull FragmentActivity fragmentActivity, String organizerEmail) {
        super(fragmentActivity);
        this.organizerEmail = organizerEmail;
    }
    // position 0 = EVENTS, position 1 = CREATE
    @Override @NonNull
    public Fragment createFragment(int position) {
        Fragment fragment;
        if (position == 0) {
            fragment = new OrganizerEventsFragment();
        } else {
            fragment = new OrganizerToolsFragment();
        }  // <-- create form

        Bundle args = new Bundle();
        args.putString("organizerEmail", organizerEmail);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public int getItemCount() {
        return PAGE_COUNT;
    }
}
