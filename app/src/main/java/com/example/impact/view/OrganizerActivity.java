package com.example.impact.view;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.example.impact.R;
import com.example.impact.view.adapter.OrganizerPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

/**
 * Hosts organizer-specific tools and an entrant preview using tabbed navigation.
 */
public class OrganizerActivity extends AppCompatActivity {

    private static final String ORGANIZER_TOOLS_TITLE = "Organizer Tools";
    private static final String ENTRANT_VIEW_TITLE = "Entrant View";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer);

        Toolbar toolbar = findViewById(R.id.organizerToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.organizer_dashboard_title);
        }

        ViewPager2 viewPager = findViewById(R.id.organizerViewPager);
        viewPager.setAdapter(new OrganizerPagerAdapter(this));

        TabLayout tabLayout = findViewById(R.id.organizerTabLayout);
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText(ORGANIZER_TOOLS_TITLE);
            } else {
                tab.setText(ENTRANT_VIEW_TITLE);
            }
        }).attach();
    }
}
