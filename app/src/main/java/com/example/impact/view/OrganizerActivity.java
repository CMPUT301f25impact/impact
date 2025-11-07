package com.example.impact.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.example.impact.R;
import com.example.impact.utils.SessionManager;
import com.example.impact.view.adapter.OrganizerPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class OrganizerActivity extends AppCompatActivity {

    private static final String EVENTS_TITLE = "Events";
    private static final String CREATE_TITLE = "Create";

    /** Optional: pass this in an Intent extra to open a tab directly. */
    public static final String EXTRA_INITIAL_TAB = "initial_tab"; // 0 = Events, 1 = Create

    private SessionManager sessionManager;
    private ViewPager2 viewPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.organizerToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.organizer_dashboard_title);
        }

        sessionManager = new SessionManager(this);

        // ViewPager + Tabs
        viewPager = findViewById(R.id.organizerViewPager);
        String organizerEmail = getIntent().getStringExtra("extra_user_email");
        viewPager.setAdapter(new OrganizerPagerAdapter(this, organizerEmail));

        TabLayout tabLayout = findViewById(R.id.organizerTabLayout);
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) ->
                tab.setText(position == 0 ? EVENTS_TITLE : CREATE_TITLE)
        ).attach();

        // Optional: open a specific tab if provided
        int initialTab = getIntent().getIntExtra(EXTRA_INITIAL_TAB, 0);
        viewPager.setCurrentItem(initialTab, false);
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

    /** Called from OrganizerEventsFragment’s “+ Create New Event” button. */
    public void goToCreateTab() {
        if (viewPager != null) viewPager.setCurrentItem(1, true);
    }

    /** If you ever need to jump back to Events from Create. */
    public void goToEventsTab() {
        if (viewPager != null) viewPager.setCurrentItem(0, true);
    }
}
