package com.example.impact.view.adapter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.impact.view.AdminEventListFragment;

/**
 * Supplies admin dashboard fragments to the ViewPager
 */
public class AdminPagerAdapter extends FragmentStateAdapter {

    private final String[] tabs;
    public AdminPagerAdapter(String[] tabs, @NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        this.tabs = tabs;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        String type = tabs[position];

        switch (type) {
            case "Events":
                return new AdminEventListFragment();
            default:
                // TODO implement rest
                return null;
        }
    }

    @Override
    public int getItemCount() {
        return tabs.length;
    }
}
