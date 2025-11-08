package com.example.impact.view.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.impact.view.AdminEventListFragment;
import com.example.impact.view.AdminImageListFragment;
import com.example.impact.view.AdminProfileListFragment;

/**
 * Supplies admin dashboard fragments to the ViewPager
 */
public class AdminPagerAdapter extends FragmentStateAdapter {

    private final String[] tabs;
    /**
     * Creates a pager adapter scoped to the admin dashboard.
     *
     * @param tabs              labels indicating which fragment to instantiate
     * @param fragmentActivity  host activity backing the pager
     */
    public AdminPagerAdapter(String[] tabs, @NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        this.tabs = tabs;
    }

    /**
     * Instantiates the fragment that corresponds to the requested tab.
     *
     * @param position pager index
     * @return fragment for the tab
     */
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
                return new AdminProfileListFragment();
        }
        // this should never be hit
        return null;
    }

    /**
     * @return number of tabs configured for the admin dashboard
     */
    @Override
    public int getItemCount() {
        return tabs.length;
    }
}
