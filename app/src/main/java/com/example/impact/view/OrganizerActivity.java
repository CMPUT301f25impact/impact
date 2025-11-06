package com.example.impact.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.example.impact.R;
import com.example.impact.view.adapter.OrganizerPagerAdapter;
import com.example.impact.utils.SessionManager;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

/**
 * Hosts organizer-specific tools and an entrant preview using tabbed navigation.
 */
public class OrganizerActivity extends AppCompatActivity {

    private static final String ORGANIZER_TOOLS_TITLE = "Organizer Tools";
    private static final String ENTRANT_VIEW_TITLE = "Entrant View";

    private SessionManager sessionManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer);

        MaterialToolbar toolbar = findViewById(R.id.organizerToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.organizer_dashboard_title);
        }

        sessionManager = new SessionManager(this);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_session, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            performLogout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void performLogout() {
        sessionManager.clearSession(() -> {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}
