package com.example.impact.view.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.impact.view.AdminEventListFragment;
import com.example.impact.view.AdminImageListFragment;

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
        System.out.println(position);
        System.out.println(type);

        switch (type) {
            case "Events":
                return new AdminEventListFragment();
            case "Images":
                return new AdminImageListFragment();
            case "Profiles":
                // TODO temporarily rendering images under profiles
                // add profiles to this tab when done (AdminProfileListFragment)
                return new AdminImageListFragment();
        }
        // this should never be hit
        return null;
    }

    @Override
    public int getItemCount() {
        return tabs.length;
    }
}
